
public void bind() throws Exception {

        // Create the root APR memory pool
        try {
            rootPool = Pool.create(0);
        } catch (UnsatisfiedLinkError e) {
            throw new Exception(sm.getString("endpoint.init.notavail"));
        }

        // Create the pool for the server socket
        serverSockPool = Pool.create(rootPool);
        // Create the APR address that will be bound
        String addressStr = null;
        if (getAddress() != null) {
            addressStr = getAddress().getHostAddress();
        }
        int family = Socket.APR_INET;
        if (Library.APR_HAVE_IPV6) {
            if (addressStr == null) {
                if (!OS.IS_BSD && !OS.IS_WIN32 && !OS.IS_WIN64)
                    family = Socket.APR_UNSPEC;
            } else if (addressStr.indexOf(':') >= 0) {
                family = Socket.APR_UNSPEC;
            }
         }

        long inetAddress = Address.info(addressStr, family,
                getPort(), 0, rootPool);
        // Create the APR server socket
        serverSock = Socket.create(Address.getInfo(inetAddress).family,
                Socket.SOCK_STREAM,
                Socket.APR_PROTO_TCP, rootPool);
        if (OS.IS_UNIX) {
            Socket.optSet(serverSock, Socket.APR_SO_REUSEADDR, 1);
        }
        // Deal with the firewalls that tend to drop the inactive sockets
        Socket.optSet(serverSock, Socket.APR_SO_KEEPALIVE, 1);
        // Bind the server socket
        int ret = Socket.bind(serverSock, inetAddress);
        if (ret != 0) {
            throw new Exception(sm.getString("endpoint.init.bind", "" + ret, Error.strerror(ret)));
        }
        // Start listening on the server socket
        ret = Socket.listen(serverSock, getBacklog());
        if (ret != 0) {
            throw new Exception(sm.getString("endpoint.init.listen", "" + ret, Error.strerror(ret)));
        }
        if (OS.IS_WIN32 || OS.IS_WIN64) {
            // On Windows set the reuseaddr flag after the bind/listen
            Socket.optSet(serverSock, Socket.APR_SO_REUSEADDR, 1);
        }

        // Enable Sendfile by default if it has not been configured but usage on
        // systems which don't support it cause major problems
        if (!useSendFileSet) {
            useSendfile = Library.APR_HAS_SENDFILE;
        } else if (useSendfile && !Library.APR_HAS_SENDFILE) {
            useSendfile = false;
        }

        // Initialize thread count default for acceptor
        if (acceptorThreadCount == 0) {
            // FIXME: Doesn't seem to work that well with multiple accept threads
            acceptorThreadCount = 1;
        }

        // Delay accepting of new connections until data is available
        // Only Linux kernels 2.4 + have that implemented
        // on other platforms this call is noop and will return APR_ENOTIMPL.
        if (deferAccept) {
            if (Socket.optSet(serverSock, Socket.APR_TCP_DEFER_ACCEPT, 1) == Status.APR_ENOTIMPL) {
                deferAccept = false;
            }
        }

        // Initialize SSL if needed
        if (isSSLEnabled()) {

            if (SSLCertificateFile == null) {
                // This is required
                throw new Exception(sm.getString("endpoint.apr.noSslCertFile"));
            }

            // SSL protocol
            int value = SSL.SSL_PROTOCOL_NONE;
            if (SSLProtocol == null || SSLProtocol.length() == 0) {
                value = SSL.SSL_PROTOCOL_ALL;
            } else {
                for (String protocol : SSLProtocol.split("\\+")) {
                    protocol = protocol.trim();
                    if ("SSLv2".equalsIgnoreCase(protocol)) {
                        value |= SSL.SSL_PROTOCOL_SSLV2;
                    } else if ("SSLv3".equalsIgnoreCase(protocol)) {
                        value |= SSL.SSL_PROTOCOL_SSLV3;
                    } else if ("TLSv1".equalsIgnoreCase(protocol)) {
                        value |= SSL.SSL_PROTOCOL_TLSV1;
                    } else if ("TLSv1.1".equalsIgnoreCase(protocol)) {
                        value |= SSL.SSL_PROTOCOL_TLSV1_1;
                    } else if ("TLSv1.2".equalsIgnoreCase(protocol)) {
                        value |= SSL.SSL_PROTOCOL_TLSV1_2;
                    } else if ("all".equalsIgnoreCase(protocol)) {
                        value |= SSL.SSL_PROTOCOL_ALL;
                    } else {
                        // Protocol not recognized, fail to start as it is safer than
                        // continuing with the default which might enable more than the
                        // is required
                        throw new Exception(sm.getString(
                                "endpoint.apr.invalidSslProtocol", SSLProtocol));
                    }
                }
            }

            // Create SSL Context
            try {
                sslContext = SSLContext.make(rootPool, value, SSL.SSL_MODE_SERVER);
            } catch (Exception e) {
                // If the sslEngine is disabled on the AprLifecycleListener
                // there will be an Exception here but there is no way to check
                // the AprLifecycleListener settings from here
                throw new Exception(
                        sm.getString("endpoint.apr.failSslContextMake"), e);
            }

            if (SSLInsecureRenegotiation) {
                boolean legacyRenegSupported = false;
                try {
                    legacyRenegSupported = SSL.hasOp(SSL.SSL_OP_ALLOW_UNSAFE_LEGACY_RENEGOTIATION);
                    if (legacyRenegSupported)
                        SSLContext.setOptions(sslContext, SSL.SSL_OP_ALLOW_UNSAFE_LEGACY_RENEGOTIATION);
                } catch (UnsatisfiedLinkError e) {
                    // Ignore
                }
                if (!legacyRenegSupported) {
                    // OpenSSL does not support unsafe legacy renegotiation.
                    log.warn(sm.getString("endpoint.warn.noInsecureReneg",
                                          SSL.versionString()));
                }
            }

            // Set cipher order: client (default) or server
            if (SSLHonorCipherOrder) {
                boolean orderCiphersSupported = false;
                try {
                    orderCiphersSupported = SSL.hasOp(SSL.SSL_OP_CIPHER_SERVER_PREFERENCE);
                    if (orderCiphersSupported)
                        SSLContext.setOptions(sslContext, SSL.SSL_OP_CIPHER_SERVER_PREFERENCE);
                } catch (UnsatisfiedLinkError e) {
                    // Ignore
                }
                if (!orderCiphersSupported) {
                    // OpenSSL does not support ciphers ordering.
                    log.warn(sm.getString("endpoint.warn.noHonorCipherOrder",
                                          SSL.versionString()));
                }
            }

            // Disable compression if requested
            if (SSLDisableCompression) {
                boolean disableCompressionSupported = false;
                try {
                    disableCompressionSupported = SSL.hasOp(SSL.SSL_OP_NO_COMPRESSION);
                    if (disableCompressionSupported)
                        SSLContext.setOptions(sslContext, SSL.SSL_OP_NO_COMPRESSION);
                } catch (UnsatisfiedLinkError e) {
                    // Ignore
                }
                if (!disableCompressionSupported) {
                    // OpenSSL does not support ciphers ordering.
                    log.warn(sm.getString("endpoint.warn.noDisableCompression",
                                          SSL.versionString()));
                }
            }

            // List the ciphers that the client is permitted to negotiate
            SSLContext.setCipherSuite(sslContext, SSLCipherSuite);
            // Load Server key and certificate
            SSLContext.setCertificate(sslContext, SSLCertificateFile, SSLCertificateKeyFile, SSLPassword, SSL.SSL_AIDX_RSA);
            // Set certificate chain file
            SSLContext.setCertificateChainFile(sslContext, SSLCertificateChainFile, false);
            // Support Client Certificates
            SSLContext.setCACertificate(sslContext, SSLCACertificateFile, SSLCACertificatePath);
            // Set revocation
            SSLContext.setCARevocation(sslContext, SSLCARevocationFile, SSLCARevocationPath);
            // Client certificate verification
            value = SSL.SSL_CVERIFY_NONE;
            if ("optional".equalsIgnoreCase(SSLVerifyClient)) {
                value = SSL.SSL_CVERIFY_OPTIONAL;
            } else if ("require".equalsIgnoreCase(SSLVerifyClient)) {
                value = SSL.SSL_CVERIFY_REQUIRE;
            } else if ("optionalNoCA".equalsIgnoreCase(SSLVerifyClient)) {
                value = SSL.SSL_CVERIFY_OPTIONAL_NO_CA;
            }
            SSLContext.setVerify(sslContext, value, SSLVerifyDepth);
            // For now, sendfile is not supported with SSL
            if (useSendfile) {
                useSendfile = false;
                if (useSendFileSet) {
                    log.warn(sm.getString("endpoint.apr.noSendfileWithSSL"));
                }
            }
        }
    }
