
package org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.1.3
 * Mon Mar 02 15:28:33 CET 2009
 * Generated source version: 2.1.3
 * 
 */

@WebFault(name = "SecurityException", targetNamespace = "urn:epcglobal:alelr:wsdl:1")
public class SecurityExceptionResponse extends Exception {
    public static final long serialVersionUID = 20090302152833L;
    
    private org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.SecurityException securityException;

    public SecurityExceptionResponse() {
        super();
    }
    
    public SecurityExceptionResponse(String message) {
        super(message);
    }
    
    public SecurityExceptionResponse(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityExceptionResponse(String message, org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.SecurityException securityException) {
        super(message);
        this.securityException = securityException;
    }

    public SecurityExceptionResponse(String message, org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.SecurityException securityException, Throwable cause) {
        super(message, cause);
        this.securityException = securityException;
    }

    public org.rifidi.edge.client.ale.api.wsdl.alelr.epcglobal.SecurityException getFaultInfo() {
        return this.securityException;
    }
}