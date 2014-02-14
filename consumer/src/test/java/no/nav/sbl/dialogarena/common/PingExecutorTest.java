package no.nav.sbl.dialogarena.common;

import no.nav.sbl.dialogarena.types.Pingable.Ping;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;

public class PingExecutorTest {

    @Test
    public void skalKunneKjorePing() {
        PingExecutor pingImplementation = new PingImplementation("testPing");
        Ping ping = pingImplementation.ping();
        
        Assert.assertTrue(ping.isVellykket());
    }
    
    @Test
    public void PingKanFeile() {
        PingExecutor pingImplementation = new PingImplementationFailing("testPingFail");
        Ping ping = pingImplementation.ping();
        
        Assert.assertFalse(ping.isVellykket());
        Assert.assertThat(ping.getAarsak(), instanceOf(RuntimeException.class));
    }
       
    private class PingImplementation extends PingExecutor {
        String componentName;
        
        public PingImplementation(String componentName) {
            super(componentName);
            this.componentName=componentName;
        }
        @Override
        public Ping ping() {
            return Ping.lyktes(componentName);
        }

        @Override
        protected void pingOperation() {
        }
    }
    
    private class PingImplementationFailing extends PingExecutor {
        String componentName;
        
        public PingImplementationFailing(String componentName) {
            super(componentName);
            this.componentName=componentName;
        }
        @Override
        public Ping ping() {
            return Ping.feilet(componentName, new RuntimeException());
        }

        @Override
        protected void pingOperation() {
        }
    }
}
