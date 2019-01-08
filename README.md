Force.com IDE Core
========

**Force.com IDE is in a maintenance-only state. We still provide support for the product through our official channels, but updates prior to October 12, 2019 will be only for critical security issues that arise. On October 12, 2019, we will no longer provide support or updates of any kind for Force.com IDE. On that date, we will also begin archiving documentation and removing download links for the product. We recommend that you start migrating to [Salesforce Extensions for Visual Studio Code](https://marketplace.visualstudio.com/items?itemName=salesforce.salesforcedx-vscode) or one of the great tools made by our partners. For more information, see [The Future of Salesforce IDEs](https://developer.salesforce.com/blogs/2018/12/the-future-of-salesforce-ides.html) on the Salesforce Developers Blog.**

**This repository will be archived on October 12, 2019. The source code will remain available in the repository, but the repository will no longer accept pull requests. To contribute to Force.com IDE after the repository is archived, create a fork. For details about archived repositories, see [About archiving repositories](https://help.github.com/articles/about-archiving-repositories/) in GitHub Help.**

This is the core part of the legacy Force.com IDE development tools for salesforce.com.

For more information, refer to the [Force.com IDE page][1].

License
-------

[Eclipse Public License (EPL) v1.0][2]

Getting Started
---------------

Refer to the wiki pages for this repository. You can access those pages
by clicking on the wiki icon in the navigation on the right side of this
page.

Building
--------

To build an external version, issue a `mvn clean package -Dexternal=true`
in this directory. If everything executes successfully, you shall see
the artifacts in com.salesforce.ide.repository.external/target/repository.

Contributing
--------
As this project is in a maintenance-only state, we will no longer accept issues or pull requests through Github. 

[1]: https://developer.salesforce.com/page/Force.com_IDE
[2]: http://wiki.eclipse.org/EPL
