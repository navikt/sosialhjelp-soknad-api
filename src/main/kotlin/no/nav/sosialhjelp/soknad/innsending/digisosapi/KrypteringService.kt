package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.instrumentation.annotations.WithSpan
import no.ks.kryptering.CMSKrypteringImpl
import no.ks.kryptering.CMSStreamKryptering
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.security.Security
import java.security.cert.X509Certificate
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Component
class KrypteringService(private val dokumentlagerClient: DokumentlagerClient) {
    private val executor = ExecutorCompletionService<Void>(Executors.newCachedThreadPool())
    private val kryptering: CMSStreamKryptering = CMSKrypteringImpl()

    private val certificate: X509Certificate get() = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate()

    @WithSpan("Doing File-encryption")
    fun krypter(
        dokumentStream: InputStream,
        krypteringFutureList: MutableList<Future<Void>>,
    ): InputStream {
        val pipedInputStream = PipedInputStream()
        try {
            val pipedOutputStream = PipedOutputStream(pipedInputStream)
            val krypteringFuture =
                executor.submit {
                    try {
                        if (MiljoUtils.isNonProduction() && MiljoUtils.isMockAltProfil()) {
                            IOUtils.copy(dokumentStream, pipedOutputStream)
                        } else {
                            kryptering.krypterData(
                                pipedOutputStream,
                                dokumentStream,
                                certificate,
                                Security.getProvider("BC"),
                            )
                        }
                    } catch (e: Exception) {
                        recordSpanError(e)
                        log.error("Encryption failed, setting exception on encrypted InputStream", e)
                        throw IllegalStateException("An error occurred during encryption", e)
                    } finally {
                        try {
                            log.debug("Closing encryption OutputStream")
                            pipedOutputStream.close()
                            log.debug("Encryption OutputStream closed")
                        } catch (e: IOException) {
                            recordSpanError(e)
                            log.error("Failed closing encryption OutputStream", e)
                        }
                    }
                    null
                }
            krypteringFutureList.add(krypteringFuture)
        } catch (e: IOException) {
            recordSpanError(e)
            throw RuntimeException(e)
        } finally {
            log.debug("Closing dokumentStream InputStream")
            dokumentStream.close()
        }
        return pipedInputStream
    }

    private fun recordSpanError(e: Throwable) {
        Span.current().recordException(e)
        Span.current().setStatus(StatusCode.ERROR)
    }

    companion object {
        private val log by logger()

        fun waitForFutures(krypteringFutureList: List<Future<Void>>) {
            for (voidFuture in krypteringFutureList) {
                runCatching { voidFuture[300, TimeUnit.SECONDS] }
                    .onFailure {
                        Span.current().recordException(it)
                        Span.current().setStatus(StatusCode.ERROR)

                        when (it) {
                            is CompletionException -> throw IllegalStateException(it.cause)
                            is ExecutionException, is TimeoutException, is InterruptedException -> throw IllegalStateException(it)
                            else -> throw it
                        }
                    }
            }
        }
    }
}
