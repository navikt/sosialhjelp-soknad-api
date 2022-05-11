package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.ks.kryptering.CMSKrypteringImpl
import no.ks.kryptering.CMSStreamKryptering
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.common.ServiceUtils
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
class KrypteringService(
    private val serviceUtils: ServiceUtils
) {
    private val executor = ExecutorCompletionService<Void>(Executors.newCachedThreadPool())
    private val kryptering: CMSStreamKryptering = CMSKrypteringImpl()

    fun krypter(
        dokumentStream: InputStream,
        krypteringFutureList: MutableList<Future<Void>>,
        fiksX509Certificate: X509Certificate
    ): InputStream {
        val pipedInputStream = PipedInputStream()
        try {
            val pipedOutputStream = PipedOutputStream(pipedInputStream)
            val krypteringFuture = executor.submit {
                try {
                    if (MiljoUtils.isNonProduction() && serviceUtils.isMockAltProfil()) {
                        IOUtils.copy(dokumentStream, pipedOutputStream)
                    } else {
                        log.info("kryptering nu!")
                        kryptering.krypterData(
                            pipedOutputStream,
                            dokumentStream,
                            fiksX509Certificate,
                            Security.getProvider("BC")
                        )
                    }
                } catch (e: Exception) {
                    log.error("Encryption failed, setting exception on encrypted InputStream", e)
                    throw IllegalStateException("An error occurred during encryption", e)
                } finally {
                    try {
                        log.debug("Closing encryption OutputStream")
                        pipedOutputStream.close()
                        log.debug("Encryption OutputStream closed")
                    } catch (e: IOException) {
                        log.error("Failed closing encryption OutputStream", e)
                    }
                }
                null
            }
            log.info("add to krypteringFutureList")
            krypteringFutureList.add(krypteringFuture)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            log.debug("Closing dokumentStream InputStream")
            dokumentStream.close()
        }
        return pipedInputStream
    }

    companion object {
        private val log by logger()

        fun waitForFutures(krypteringFutureList: List<Future<Void>>) {
            for (voidFuture in krypteringFutureList) {
                try {
                    voidFuture[300, TimeUnit.SECONDS]
                } catch (e: CompletionException) {
                    throw IllegalStateException(e.cause)
                } catch (e: ExecutionException) {
                    throw IllegalStateException(e)
                } catch (e: TimeoutException) {
                    throw IllegalStateException(e)
                } catch (e: InterruptedException) {
                    throw IllegalStateException(e)
                }
            }
        }
    }
}
