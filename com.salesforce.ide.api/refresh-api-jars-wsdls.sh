APP_ROOT="../../../../../../../160-patch";
if [ $# -gt 0 -a -z "$1" ]; then
  APP_ROOT=$1
fi

echo App root here: ${APP_ROOT}
echo
echo 'Note: Ensure that the WSC module ('${APP_ROOT}'/core/wsc) source and classes are up-to-date.'
echo

echo "Metadata..."
echo ' jar...'
p4 edit lib/sfdc_metadatawsdl.jar
echo Before update...
ls -Llsrt $APP_ROOT/core/sfdc-test/func/java/ext-gen/sfdc_metadatawsdl.jar lib/sfdc_metadatawsdl.jar
echo 'cp '${APP_ROOT}'/core/sfdc-test/func/java/ext-gen/sfdc_metadatawsdl.jar lib/sfdc_metadatawsdl.jar'
cp $APP_ROOT/core/sfdc-test/func/java/ext-gen/sfdc_metadatawsdl.jar lib/sfdc_metadatawsdl.jar
echo After...
ls -Llsrt lib/sfdc_metadatawsdl.jar
echo ' wsdl...'
ls -Llsrt wsdl/metadata.wsdl
p4 edit wsdl/metadata.wsdl
echo 'cp '${APP_ROOT}'/core/sfdc-test/func/wsdl/metadata.wsdl wsdl/metadata.wsdl'
cp $APP_ROOT/core/sfdc-test/func/wsdl/metadata.wsdl wsdl/metadata.wsdl
ls -Llsrt wsdl/metadata.wsdl
echo

echo "Enterprise..."
echo ' wsdl...'
# wsc classes must exist
if [ ! -d $APP_ROOT/core/wsc/java/classes/com ]; then
  echo ${APP_ROOT}'/core/wsc/java/classes/com not found'
  exit -1
fi

p4 edit lib/wsc.jar
echo Before update...
ls -Llsrt lib/wsc.jar
echo 'jar cvf lib/wsc.jar -C '${APP_ROOT}'/core/wsc/java/classes .'
jar cvf lib/wsc.jar -C ${APP_ROOT}/core/wsc/java/classes .
echo After...
ls -Llsrt lib/wsc.jar
echo ' wsdl...'
ls -Llsrt wsdl/enterprise.wsdl
p4 edit wsdl/enterprise.wsdl
echo 'cp '${APP_ROOT}'/core/sfdc-test/func/wsdl/enterprise.wsdl wsdl/enterprise.wsdl'
cp $APP_ROOT/core/sfdc-test/func/wsdl/enterprise.wsdl wsdl/enterprise.wsdl
ls -Llsrt wsdl/enterprise.wsdl
echo

echo "Apex..."
echo ' jar...'
p4 edit lib/apexwsdl.jar
echo Before update...
ls -Llsrt $APP_ROOT/core/shared/java/ext-gen/apexwsdl.jar lib/apexwsdl.jar
echo 'cp '${APP_ROOT}'/core/shared/java/ext-gen/apexwsdl.jar lib/apexwsdl.jar'
cp $APP_ROOT/core/shared/java/ext-gen/apexwsdl.jar lib/apexwsdl.jar
echo After...
ls -Llsrt lib/apexwsdl.jar
echo ' wsdl...'
ls -Llsrt wsdl/apex.wsdl
p4 edit wsdl/apex.wsdl
echo 'cp '${APP_ROOT}'/core/sfdc-test/func/wsdl/apex.wsdl wsdl/apex.wsdl'
cp $APP_ROOT/core/sfdc-test/func/wsdl/apex.wsdl wsdl/apex.wsdl
ls -Llsrt wsdl/apex.wsdl
echo

echo "Partner..."
echo ' jar...'
p4 edit lib/wsc_partnerwsdl.jar
echo Before update...
ls -Llsrt $APP_ROOT/core/shared/java/ext-gen/wsc_partnerwsdl.jar lib/wsc_partnerwsdl.jar
echo 'cp '${APP_ROOT}'/core/shared/java/ext-gen/wsc_partnerwsdl.jar lib/wsc_partnerwsdl.jar'
cp $APP_ROOT/core/shared/java/ext-gen/wsc_partnerwsdl.jar lib/wsc_partnerwsdl.jar
echo After...
ls -Llsrt lib/wsc_partnerwsdl.jar
echo ' wsdl...'
ls -Llsrt wsdl/partner.wsdl
p4 edit wsdl/partner.wsdl
echo 'cp '${APP_ROOT}'/core/sfdc-test/func/wsdl/apex.wsdl wsdl/partner.wsdl'
cp $APP_ROOT/core/sfdc-test/func/wsdl/apex.wsdl wsdl/partner.wsdl
ls -Llsrt wsdl/partner.wsdl

echo 'Done'
