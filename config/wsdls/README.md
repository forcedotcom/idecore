### Introduction

This is the [Gradle](http://www.gradle.org) project for generating and copying the necessary WSDL jars for the Force.com IDE.

### Usage

1. Ensure that you have a 1.7 version of the JDK installed. 1.8 might work but it has not been tested.
2. Place the latest versions of the WSDL files into src/main/resources directory
3. If a particular WSDL needs any special configurations, modify build.gradle
4. Execute `gradlew` from this directory. It will generate the files and copy them over.


Instruction for generation of ide.core/com.salesforce.ide.api/schema/metadata.xsd

1. Generate Metadata WSDL
 1a. Start latest version of server, locally
 1b. Navigate to metadata.wsdl generator on local instance
      https://<LOCAL-MACHINE>.salesforce.com:<LOCAL-CONFIGURED-PORT>//services/wsdl/metadata
 1c. Copy WSDL Text 
2. Open ide.core/com.salesforce.ide.api/schema/Metadata.xsd in Eclipse Force.comIDE and replace entire file with clipboard test
3. Delete all text prior to "<xsd:schema ..." statement
4. Delete all text follwoing </xsd:schema ..." statement
5. Generate XSD postfix
  5a. Navigate to metadata XSD generator on local instance
      https://<LOCAL-MACHINE>.salesforce.com:<LOCAL-CONFIGURED-PORT>//services/wsdl/metadata?xsd
  5b. Copy XSD Text
6. In Metadata.xsd: Paste text before "</xml:shema" statement which is now last line in file
7. replace initial "<xml:Schmea..." line of file with following 
  <?xml version="1.0" encoding="UTF-8"?>
<!--
Salesforce.com Metadata XSD for API version 36.0

Copyright 2006-2016 Salesforce.com, inc. All Rights Reserved
-->
<xsd:schema xmlns:tns="http://soap.sforce.com/2006/04/metadata"
   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
   jaxb:version="2.0" targetNamespace="http://soap.sforce.com/2006/04/metadata"
   elementFormDefault="qualified">
8. Press Shift-Command-'F' (on MAC) Shift-Ctrl-'F' (on Linux) to format file correctly
9. Save File
