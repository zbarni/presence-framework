
package org.mobicents.as7;

import java.io.Serializable;
import java.util.Arrays;
import javax.annotation.Generated;
import org.jboss.vfs.VirtualFile;


/**
 * Warning this class consists of generated code.
 * 
 */
@Generated(value = "org.jboss.logging.processor.model.MessageBundleImplementor", date = "2014-02-09T00:39:09+0100")
public class SipMessages_$bundle
    implements Serializable, SipMessages
{

    private final static long serialVersionUID = 1L;
    private final static String projectCode = "MSS";
    public final static SipMessages_$bundle INSTANCE = new SipMessages_$bundle();
    private final static String failedToResolveModule = "Failed to resolve module for deployment %s";
    private final static String wrongComponentType = "%s has the wrong component type, it cannot be used as a SIP component";
    private final static String noMetricsAvailable = "No metrics available";
    private final static String connectorStartError = "Error starting web connector";
    private final static String nullValue = "Null service value";
    private final static String failedToGetMetrics = "Failed to get metrics %s";
    private final static String failedToAddSipDeployment = "Failed to add Mobicents SIP deployment service";
    private final static String errorStartingSip = "Error starting sip container";
    private final static String unknownMetric = "Unknown metric %s";

    protected SipMessages_$bundle() {
    }

    protected Object readResolve() {
        return INSTANCE;
    }

    public final String failedToResolveModule(final VirtualFile deploymentRoot) {
        String result = String.format(((projectCode +"018026: ")+ failedToResolveModule$str()), deploymentRoot);
        return result;
    }

    protected String failedToResolveModule$str() {
        return failedToResolveModule;
    }

    public final RuntimeException wrongComponentType(final String clazz) {
        RuntimeException result = new RuntimeException(String.format(((projectCode +"018043: ")+ wrongComponentType$str()), clazz));
        StackTraceElement[] st = result.getStackTrace();
        result.setStackTrace(Arrays.copyOfRange(st, 1, st.length));
        return result;
    }

    protected String wrongComponentType$str() {
        return wrongComponentType;
    }

    public final String noMetricsAvailable() {
        String result = String.format(((projectCode +"018003: ")+ noMetricsAvailable$str()));
        return result;
    }

    protected String noMetricsAvailable$str() {
        return noMetricsAvailable;
    }

    public final String connectorStartError() {
        String result = String.format(((projectCode +"018007: ")+ connectorStartError$str()));
        return result;
    }

    protected String connectorStartError$str() {
        return connectorStartError;
    }

    public final IllegalStateException nullValue() {
        IllegalStateException result = new IllegalStateException(String.format(((projectCode +"018008: ")+ nullValue$str())));
        StackTraceElement[] st = result.getStackTrace();
        result.setStackTrace(Arrays.copyOfRange(st, 1, st.length));
        return result;
    }

    protected String nullValue$str() {
        return nullValue;
    }

    public final String failedToGetMetrics(final String reason) {
        String result = String.format(((projectCode +"018002: ")+ failedToGetMetrics$str()), reason);
        return result;
    }

    protected String failedToGetMetrics$str() {
        return failedToGetMetrics;
    }

    public final String failedToAddSipDeployment() {
        String result = String.format(((projectCode +"018027: ")+ failedToAddSipDeployment$str()));
        return result;
    }

    protected String failedToAddSipDeployment$str() {
        return failedToAddSipDeployment;
    }

    public final String errorStartingSip() {
        String result = String.format(((projectCode +"018009: ")+ errorStartingSip$str()));
        return result;
    }

    protected String errorStartingSip$str() {
        return errorStartingSip;
    }

    public final String unknownMetric(final Object metric) {
        String result = String.format(((projectCode +"018098: ")+ unknownMetric$str()), metric);
        return result;
    }

    protected String unknownMetric$str() {
        return unknownMetric;
    }

}
