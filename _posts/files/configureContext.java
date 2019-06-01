```java

private void configureContext(WebXml webxml) {
      // As far as possible, process in alphabetical order so it is easy to
      // check everything is present
      // Some validation depends on correct public ID
      context.setPublicId(webxml.getPublicId());

      // Everything else in order
      context.setEffectiveMajorVersion(webxml.getMajorVersion());
      context.setEffectiveMinorVersion(webxml.getMinorVersion());

      for (Entry<String, String> entry : webxml.getContextParams().entrySet()) {
          context.addParameter(entry.getKey(), entry.getValue());
      }
      context.setDenyUncoveredHttpMethods(
              webxml.getDenyUncoveredHttpMethods());
      context.setDisplayName(webxml.getDisplayName());
      context.setDistributable(webxml.isDistributable());
      for (ContextLocalEjb ejbLocalRef : webxml.getEjbLocalRefs().values()) {
          context.getNamingResources().addLocalEjb(ejbLocalRef);
      }
      for (ContextEjb ejbRef : webxml.getEjbRefs().values()) {
          context.getNamingResources().addEjb(ejbRef);
      }
      for (ContextEnvironment environment : webxml.getEnvEntries().values()) {
          context.getNamingResources().addEnvironment(environment);
      }
      for (ErrorPage errorPage : webxml.getErrorPages().values()) {
          context.addErrorPage(errorPage);
      }
      for (FilterDef filter : webxml.getFilters().values()) {
          if (filter.getAsyncSupported() == null) {
              filter.setAsyncSupported("false");
          }
          context.addFilterDef(filter);
      }
      for (FilterMap filterMap : webxml.getFilterMappings()) {
          context.addFilterMap(filterMap);
      }
      context.setJspConfigDescriptor(webxml.getJspConfigDescriptor());
      for (String listener : webxml.getListeners()) {
          context.addApplicationListener(listener);
      }
      for (Entry<String, String> entry :
              webxml.getLocaleEncodingMappings().entrySet()) {
          context.addLocaleEncodingMappingParameter(entry.getKey(),
                  entry.getValue());
      }
      // Prevents IAE
      if (webxml.getLoginConfig() != null) {
          context.setLoginConfig(webxml.getLoginConfig());
      }
      for (MessageDestinationRef mdr :
              webxml.getMessageDestinationRefs().values()) {
          context.getNamingResources().addMessageDestinationRef(mdr);
      }

      // messageDestinations were ignored in Tomcat 6, so ignore here

      context.setIgnoreAnnotations(webxml.isMetadataComplete());
      for (Entry<String, String> entry :
              webxml.getMimeMappings().entrySet()) {
          context.addMimeMapping(entry.getKey(), entry.getValue());
      }
      // Name is just used for ordering
      for (ContextResourceEnvRef resource :
              webxml.getResourceEnvRefs().values()) {
          context.getNamingResources().addResourceEnvRef(resource);
      }
      for (ContextResource resource : webxml.getResourceRefs().values()) {
          context.getNamingResources().addResource(resource);
      }
      boolean allAuthenticatedUsersIsAppRole =
              webxml.getSecurityRoles().contains(
                      SecurityConstraint.ROLE_ALL_AUTHENTICATED_USERS);
      for (SecurityConstraint constraint : webxml.getSecurityConstraints()) {
          if (allAuthenticatedUsersIsAppRole) {
              constraint.treatAllAuthenticatedUsersAsApplicationRole();
          }
          context.addConstraint(constraint);
      }
      for (String role : webxml.getSecurityRoles()) {
          context.addSecurityRole(role);
      }
      for (ContextService service : webxml.getServiceRefs().values()) {
          context.getNamingResources().addService(service);
      }
      for (ServletDef servlet : webxml.getServlets().values()) {
          Wrapper wrapper = context.createWrapper();
          // Description is ignored
          // Display name is ignored
          // Icons are ignored

          // jsp-file gets passed to the JSP Servlet as an init-param

          if (servlet.getLoadOnStartup() != null) {
              wrapper.setLoadOnStartup(servlet.getLoadOnStartup().intValue());
          }
          if (servlet.getEnabled() != null) {
              wrapper.setEnabled(servlet.getEnabled().booleanValue());
          }
          wrapper.setName(servlet.getServletName());
          Map<String,String> params = servlet.getParameterMap();
          for (Entry<String, String> entry : params.entrySet()) {
              wrapper.addInitParameter(entry.getKey(), entry.getValue());
          }
          wrapper.setRunAs(servlet.getRunAs());
          Set<SecurityRoleRef> roleRefs = servlet.getSecurityRoleRefs();
          for (SecurityRoleRef roleRef : roleRefs) {
              wrapper.addSecurityReference(
                      roleRef.getName(), roleRef.getLink());
          }
          wrapper.setServletClass(servlet.getServletClass());
          MultipartDef multipartdef = servlet.getMultipartDef();
          if (multipartdef != null) {
              if (multipartdef.getMaxFileSize() != null &&
                      multipartdef.getMaxRequestSize()!= null &&
                      multipartdef.getFileSizeThreshold() != null) {
                  wrapper.setMultipartConfigElement(new MultipartConfigElement(
                          multipartdef.getLocation(),
                          Long.parseLong(multipartdef.getMaxFileSize()),
                          Long.parseLong(multipartdef.getMaxRequestSize()),
                          Integer.parseInt(
                                  multipartdef.getFileSizeThreshold())));
              } else {
                  wrapper.setMultipartConfigElement(new MultipartConfigElement(
                          multipartdef.getLocation()));
              }
          }
          if (servlet.getAsyncSupported() != null) {
              wrapper.setAsyncSupported(
                      servlet.getAsyncSupported().booleanValue());
          }
          wrapper.setOverridable(servlet.isOverridable());
          context.addChild(wrapper);
      }
      for (Entry<String, String> entry :
              webxml.getServletMappings().entrySet()) {
          context.addServletMapping(entry.getKey(), entry.getValue());
      }
      SessionConfig sessionConfig = webxml.getSessionConfig();
      if (sessionConfig != null) {
          if (sessionConfig.getSessionTimeout() != null) {
              context.setSessionTimeout(
                      sessionConfig.getSessionTimeout().intValue());
          }
          SessionCookieConfig scc =
              context.getServletContext().getSessionCookieConfig();
          scc.setName(sessionConfig.getCookieName());
          scc.setDomain(sessionConfig.getCookieDomain());
          scc.setPath(sessionConfig.getCookiePath());
          scc.setComment(sessionConfig.getCookieComment());
          if (sessionConfig.getCookieHttpOnly() != null) {
              scc.setHttpOnly(sessionConfig.getCookieHttpOnly().booleanValue());
          }
          if (sessionConfig.getCookieSecure() != null) {
              scc.setSecure(sessionConfig.getCookieSecure().booleanValue());
          }
          if (sessionConfig.getCookieMaxAge() != null) {
              scc.setMaxAge(sessionConfig.getCookieMaxAge().intValue());
          }
          if (sessionConfig.getSessionTrackingModes().size() > 0) {
              context.getServletContext().setSessionTrackingModes(
                      sessionConfig.getSessionTrackingModes());
          }
      }

      // Context doesn't use version directly

      for (String welcomeFile : webxml.getWelcomeFiles()) {
          /*
           * The following will result in a welcome file of "" so don't add
           * that to the context
           * <welcome-file-list>
           *   <welcome-file/>
           * </welcome-file-list>
           */
          if (welcomeFile != null && welcomeFile.length() > 0) {
              context.addWelcomeFile(welcomeFile);
          }
      }

      // Do this last as it depends on servlets
      for (JspPropertyGroup jspPropertyGroup :
              webxml.getJspPropertyGroups()) {
          String jspServletName = context.findServletMapping("*.jsp");
          if (jspServletName == null) {
              jspServletName = "jsp";
          }
          if (context.findChild(jspServletName) != null) {
              for (String urlPattern : jspPropertyGroup.getUrlPatterns()) {
                  context.addServletMapping(urlPattern, jspServletName, true);
              }
          } else {
              if(log.isDebugEnabled()) {
                  for (String urlPattern : jspPropertyGroup.getUrlPatterns()) {
                      log.debug("Skiping " + urlPattern + " , no servlet " +
                              jspServletName);
                  }
              }
          }
      }

      for (Entry<String, String> entry :
              webxml.getPostConstructMethods().entrySet()) {
          context.addPostConstructMethod(entry.getKey(), entry.getValue());
      }

      for (Entry<String, String> entry :
          webxml.getPreDestroyMethods().entrySet()) {
          context.addPreDestroyMethod(entry.getKey(), entry.getValue());
      }
  }

```
