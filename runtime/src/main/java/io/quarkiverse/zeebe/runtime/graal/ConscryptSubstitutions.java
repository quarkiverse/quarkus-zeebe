package io.quarkiverse.zeebe.runtime.graal;

import java.util.function.BooleanSupplier;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className = "org.apache.hc.client5.http.ssl.ConscryptClientTlsStrategy", onlyWith = ConscryptMissingSelector.class)
final class Target_org_apache_hc_client5_http_ssl_ConscryptClientTlsStrategy {

    @Substitute
    void applyParameters(final javax.net.ssl.SSLEngine sslEngine, final javax.net.ssl.SSLParameters sslParameters,
            final String[] appProtocols) {
        sslParameters.setApplicationProtocols(appProtocols);
        sslEngine.setSSLParameters(sslParameters);
    }

    @Substitute
    org.apache.hc.core5.reactor.ssl.TlsDetails createTlsDetails(final javax.net.ssl.SSLEngine sslEngine) {
        return null;
    }
}

final class ConscryptMissingSelector implements BooleanSupplier {

    @Override
    public boolean getAsBoolean() {
        try {
            Class.forName("org.conscrypt.Conscrypt");
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }
}

public class ConscryptSubstitutions {

}
