/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.nbandroid.netbeans.gradle.v2.layout.values;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import org.netbeans.api.xml.cookies.CookieMessage;
import org.netbeans.api.xml.cookies.CookieObserver;
import org.netbeans.api.xml.parsers.SAXEntityParser;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DefaultXMLProcessorDetail;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author arsi
 */
public class ResourceXsdValidateXMLSupport extends ValidateXMLSupport {

    private CookieObserver console;
    private final InputSource inputSource;
    private final int mode = CheckXMLSupport.DOCUMENT_MODE;
    // fatal error counter
    private int fatalErrors;
    private boolean bogusSchemaRequest;
    private boolean reportBogusSchemaRequest = false;
    private final String xsd;
    // error counter
    private int errors;
    // error locator or null
    private Locator locator;

    public ResourceXsdValidateXMLSupport(InputSource inputSource, URL xsdUrl) {
        super(inputSource);
        this.inputSource = inputSource;
        if (xsdUrl != null) {
            this.xsd = xsdUrl.toString();
        } else {
            this.xsd = null;
        }
    }

    @Override
    public boolean validateXML(CookieObserver l) {
        try {
            console = l;

            if (mode != CheckXMLSupport.DOCUMENT_MODE) {
                return false;
            } else {
                parse(true);
                return errors == 0 && fatalErrors == 0;
            }
        } finally {
            console = null;
            locator = null;
        }
    }

    /**
     * Perform parsing in current thread.
     */
    private void parse(boolean validate) {

        fatalErrors = 0;
        errors = 0;

        Method create = null;
        Method reset = null;

        String checkedFile = inputSource.getSystemId();

        Handler handler = new Handler();

        InputSource input = null;

        try {
            XMLReader parser = createParser(validate);
            if (parser == null) {
                fatalErrors++;
                console.receive(new CookieMessage(
                        "cannot_create_parser",
                        CookieMessage.FATAL_ERROR_LEVEL
                ));
                return;
            }

            if (validate && xsd != null) {
                input = createInputSource();
                parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", xsd); //NOI18N
            } else {
                input = createInputSource();
            }

            parser.setErrorHandler(handler);
            parser.setContentHandler(handler);

            // parse
            if (mode == CheckXMLSupport.CHECK_ENTITY_MODE) {
                new SAXEntityParser(parser, true).parse(input);
            } else if (mode == CheckXMLSupport.CHECK_PARAMETER_ENTITY_MODE) {
                new SAXEntityParser(parser, false).parse(input);
            } else {
                parser.parse(input);
            }

        } catch (SAXException ex) {
        } catch (IOException ex) {
            handler.fatalError(new SAXParseException(ex.getLocalizedMessage(), locator, ex));

        } catch (RuntimeException ex) {

            handler.runtimeError(ex);
        } finally {
        }

    }

    private void sendMessage(String message) {
        if (console != null) {
            console.receive(new CookieMessage(message));
        }
    }

    public final String getString(String key) {
        try {
            if (key == null) {
                throw new NullPointerException();
            }
            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            Class<?> cls = loader.loadClass("org.netbeans.spi.xml.cookies.Util");
            return NbBundle.getMessage(cls, key);
        } catch (ClassNotFoundException ex) {
            throw new UnsupportedOperationException("XML support not found!");
        }
    }

    public final String getString(String key, Object param1, Object param2) {
        try {
            if (key == null) {
                throw new NullPointerException();
            }
            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            Class<?> cls = loader.loadClass("org.netbeans.spi.xml.cookies.Util");
            return NbBundle.getMessage(cls, key, param1, param2);
        } catch (ClassNotFoundException ex) {
            throw new UnsupportedOperationException("XML support not found!");
        }
    }

    public final String getString(String key, Object param) {
        try {
            if (key == null) {
                throw new NullPointerException();
            }
            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            Class<?> cls = loader.loadClass("org.netbeans.spi.xml.cookies.Util");
            return NbBundle.getMessage(cls, key, param);
        } catch (ClassNotFoundException ex) {
            throw new UnsupportedOperationException("XML support not found!");
        }
    }

    private class Handler extends DefaultHandler {

        @Override
        public void warning(SAXParseException ex) {

            // heuristics to detect bogus schema loading requests
            String msg = ex.getLocalizedMessage();
            if (bogusSchemaRequest) {
                bogusSchemaRequest = false;
                if (msg != null && msg.indexOf("schema_reference.4") != -1) {   // NOI18N
                    if (reportBogusSchemaRequest) {
                        reportBogusSchemaRequest = false;
                    } else {
                        return;
                    }
                }
            }

            CookieMessage message = new CookieMessage(
                    msg,
                    CookieMessage.WARNING_LEVEL,
                    new DefaultXMLProcessorDetail(ex)
            );
            if (console != null) {
                console.receive(message);
            }
        }

        /**
         * Report maximally getMaxErrorCount() errors then stop the parser.
         */
        @Override
        public void error(SAXParseException ex) throws SAXException {
            if (errors++ == getMaxErrorCount()) {
                String msg = getString("MSG_too_many_errs");
                sendMessage(msg);
                throw ex; // stop the parser
            } else {
                CookieMessage message = new CookieMessage(
                        ex.getLocalizedMessage(),
                        CookieMessage.ERROR_LEVEL,
                        new DefaultXMLProcessorDetail(ex)
                );
                if (console != null) {
                    console.receive(message);
                }
            }
        }

        /**
         * Log runtime exception cause
         */
        private void runtimeError(RuntimeException ex) {

            // probably an internal parser error
            String msg = getString("EX_parser_ierr", ex.getMessage());
            fatalError(new SAXParseException(msg, ResourceXsdValidateXMLSupport.this.locator, ex));
        }

        @Override
        public void fatalError(SAXParseException ex) {
            fatalErrors++;
            CookieMessage message = new CookieMessage(
                    ex.getLocalizedMessage(),
                    CookieMessage.FATAL_ERROR_LEVEL,
                    new DefaultXMLProcessorDetail(ex)
            );
            if (console != null) {
                console.receive(message);
            }
        }

        public void setDocumentLocator(Locator locator) {
            ResourceXsdValidateXMLSupport.this.locator = locator;
        }

        private int getMaxErrorCount() {
            return 20;  //??? load from option
        }

    }

}
