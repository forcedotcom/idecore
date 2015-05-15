<?xml version="1.0" encoding="UTF-8"?>
<Profile xmlns="http://soap.sforce.com/2006/04/metadata">
    <applicationVisibilities>
        <application>SampleApp2</application>
        <default>false</default>
        <visible>false</visible>
    </applicationVisibilities>
    <applicationVisibilities>
        <application>SampleCustomApplication</application>
        <default>false</default>
        <visible>true</visible>
    </applicationVisibilities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.Alpha1__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.Beta__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.Checkbox1__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.Checkbox2__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>false</editable>
        <field>SampleCustomObject__c.Field1__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.HasExtId__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.IndexedCheckboxTrue__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.IndexedCheckbox__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.LongLongTextArea__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.PicklistX__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.PicklistY__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.RequiredNonUniqueIndex__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.UniqueCaseInsensitive__c</field>
    </fieldLevelSecurities>
    <fieldLevelSecurities>
        <editable>true</editable>
        <field>SampleCustomObject__c.UniqueCaseSensitive__c</field>
    </fieldLevelSecurities>
    <layoutAssingments>
        <layout>Account-Account Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Asset-Asset Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Campaign-Campaign Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Case-Case Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>CaseClose-Close Case Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Contact-Contact Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Contract-Contract Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Event-Event Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Idea-Idea Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Lead-Lead Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Opportunity-Opportunity Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>OpportunityLineItem-Opportunity Product Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Product2-Product Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>SampleCustomObject__c-SampleCustomObject1 Layout</layout>
        <recordType>SampleCustomObject__c.RT1</recordType>
    </layoutAssingments>
    <layoutAssingments>
        <layout>SampleCustomObject__c-SampleCustomObject1 Layout</layout>
        <recordType>SampleCustomObject__c.RT2</recordType>
    </layoutAssingments>
    <layoutAssingments>
        <layout>SampleCustomObject__c-SampleCustomObject1 Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Solution-Solution Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>Task-Task Layout</layout>
    </layoutAssingments>
    <layoutAssingments>
        <layout>User-User Layout</layout>
    </layoutAssingments>
    <objectPermissions>
        <object>SampleCustomObject__c</object>
        <revokeDelete>true</revokeDelete>
    </objectPermissions>
    <recordTypeVisibilities>
        <default>false</default>
        <recordType>SampleCustomObject__c.RT1</recordType>
        <visible>false</visible>
    </recordTypeVisibilities>
    <recordTypeVisibilities>
        <default>true</default>
        <recordType>SampleCustomObject__c.RT2</recordType>
        <visible>true</visible>
    </recordTypeVisibilities>
    <tabVisibilities>
        <tab>SampleCustomObject__c</tab>
        <visibility>DefaultOff</visibility>
    </tabVisibilities>
    <tabVisibilities>
        <tab>SampleSControlTab</tab>
        <visibility>DefaultOn</visibility>
    </tabVisibilities>
    <tabVisibilities>
        <tab>URLyTab</tab>
        <visibility>DefaultOn</visibility>
    </tabVisibilities>
</Profile>