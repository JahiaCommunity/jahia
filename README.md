Branch created to explore a CQRS solution based on ElasticSearch/ES (for live query part); keeping all existing JCR system for Command (Edit) part...

Interessting article:
https://culttt.com/2015/01/14/command-query-responsibility-segregation-cqrs/

The idea (still to be proved in this POC), is to keep the Edit mode as-is and keep the content stored in default JCR workspace.
BUT when publishing a content, use ES RichClient API to send it to ES cluster.

To keep retro compatibility with existing Jahia version and projets, the implementation of taglib (JCR...) will be reimplemented to use ES (in Live mode at minimum).
For a complete refactoring, it could be necessary to have a full implementation of JCR low level APIs to request to ES (for live/preview modes context only)

Important : the idea is only to replace the JCR queries part/implementation in Live/Preview modes, 
but keeping all exiting Renders/filters Jahia mechanisms for example (to keep JAhia power and retro-compatiblity)

Security: A first version could be tested without ACL mechanism; ACL could be reimplemented after via a filter mechanisme on ES data (ACL stored in ES too)

REM: this dev will be tested on Jahia Community version only!

Some potential advantages : 
---------------------------
-Jahia nodes only for Edition; ElasticSearch nodes for Browsing nodes (no complete version of Jahia needed)
=> sort of basic "cluster" implementation for Community version :-)
=> highly scalablen, resilient and maintainable (based on ES cluster)
=> easier to upgrade (browsing nodes = ES cluster + a few libs)
-Great performances! 
-could be used directly in headless (JS...) via ES rest API.
-...

Anyone interrested to help test this? -> contact me ;-)
