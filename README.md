Standard Floss Physical Real Time Availability Plugin for Ex Libris® Primo™
===========================================================================

[Standard Floss Physical RTA plugin] is a plugin for [Primo]™, the proprietary
discovery tool of [Ex Libris]®, that allows to check in real time the
availability of an item, usually a book or a journal, on remote systems that
implement the [ILS-DI] standard (Integrated library system - Discovery
interface) of the [Digital Library Federation].

Without this plugin, [Primo]™ can check only items of other softwares of
[Ex Libris]®. With it, [Primo]™ can check availability on many open source
softwares, as [Koha] and some other proprietary softwares.

The supported version of the ILS-DI standard is release 1.1 of December 8, 2008.


Limits of an open source plugin for a proprietary software
----------------------------------------------------------

Ex Libris® is one of the signers of the Berkeley Accord for the release 1.1 of
the standard and an official newsletter[1] of 2009 explains that Ex Libris®
wants  to replace progressively its proprietary APIs by open and standardized
ones.

So this plugin may become useless in an undetermined future. Moreover, it can be
broken at any time and without any warning by any update of the proprietary
Primo API.

Furthermore, it uses a pseudo-code for the Primo Common API, that is needed to
make the plugin works, but whose binary, source and full documentation are not
freely available. In practice, the virtual RTAplugin has been developed from the
partial informations published on the [RTA page] of Primo™.

So this is a beta release. All described features work, but have not been
checked on a live system. A final release will be published as soon as a true
access to  the Primo API will be available (at least the binary).

Note for developers: because the public documentation of the closed and
proprietary API  of Primo™ is incomplete (even the version of Java used by
Primo™ (1.6) is unknown, not publicly available and forbidden to be sought),
some checks, loops, structures, imports, etc. may be useless or not exactly or
optimally integrated.

Finally, main of the code is not specific to ILS-DI. In fact, the plugin creates
an url to check availability of requested records on the remote systems and send
the response to Primo™. Any other standard can be implemented with little
changes.


Installation
------------

Follow the instructions detailed on the [RTA page].

To test with or without the Primo API jar, just exclude virtual API and change
the build path. There is a test environment in the Primo API, but no details on
how to use it.


Configuration
-------------

To be used, some parameters should be set. First, the base url to request rta of
a record on the remote system should be set in the Institution params, for
example for Koha: "https://www.example.com/cgi-bin/koha/ilsdi.pl". Second, in
the Plugins Parameters Mapping Table, the first two values below are required
(but are not used directly by the plugin). Next ones are optional.

- SourceSystem: The standard Floss ILS-DI registered in each record, for example
  "ILS-DI". This value should be the same than in PNX records, at "record/control/sourcesystem".
- IdentifierXpath: The path in the PNX record to the id of this record on the
  remote system, for example "record/control/sourcerecordid".
- Debug: If true, more log will be written in Primo logs.
- ConnectionTimeout: Default is 1000 milliseconds.
- ReadTimeout: Default is 1000 milliseconds.
- IdType: Define if the record id passed to the remote system is a "bib" or an
  "item" (default). It depend on the IdentifierXpath.
- ReturnType: Define the level of the response of the remote system. It can be
  "bib" or "item" (default). It can't be "bib" if IdType is "item".
- ReturnFmt: Empty is the default, for Simple Availability. Any supported format
  can be used. This parameter is currently unmanaged.

Note on Item level: There may be some inconsistencies between ILS-DI and Primo
API, whose documentation is not clear on that point. ILS-DI standard doesn't
require to return the location and this location is not always the library, so
the response can't be checked directly against the mapping of names of libraries
in ILS and in Primo. Nevertheless, the item level is supported as long as the
requested type is an item (this is the default), because the library code is not
used: the item code and the RTA base url are enough.


Mapping between ILS-DI and Primo API
------------------------------------

ILS-DI knows four status of availability, but Primo API knows only three. So an
inaccurate mapping is needed, as below:

- Unknown => Check holdings
- Available => Available
- Not available => Unavailable
- Possibly available => Check holdings


Troubleshooting
---------------

Check and use the [plugin issues] page on GitHub.


Warning
-------

Use it at your own risk.

It's always recommended to backup your files and database regularly so you can
roll back if needed.


License
-------

This plugin is published under the BSD-like [CeCILL-B] licence, whose preamble
is as follows.

This Agreement is an open source software license intended to give users
significant freedom to modify and redistribute the software licensed hereunder.

The exercising of this freedom is conditional upon a strong obligation of giving
credits for everybody that distributes a software incorporating a software ruled
by the current license so as all contributions to be properly identified and
acknowledged.

In consideration of access to the source code and the rights to copy, modify and
redistribute granted by the license, users are provided only with a limited
warranty and the software's author, the holder of the economic rights, and the
successive licensors only have limited liability.

In this respect, the risks associated with loading, using, modifying and/or
developing or reproducing the software by the user are brought to the user's
attention, given its Free Software status, which may make it complicated to use,
with the result that its use is reserved for developers and experienced
professionals having in-depth computer knowledge. Users are therefore encouraged
to load and test the suitability of the software as regards their requirements
in conditions enabling the security of their systems and/or data to be ensured
and, more generally, to use and operate it in the same conditions of security.
This Agreement may be freely reproduced and published, provided it is not
altered, and that no provisions are either added or removed herefrom.

This Agreement may apply to any or all software for which the holder of the
economic rights decides to submit the use thereof to its provisions.


Contact
-------

Current maintainers:

* Daniel Berthereau (see [Daniel-KM] on GitHub)

First version of this plugin has been built for a personal study.


Copyright
---------

* Copyright Daniel Berthereau, 2015


[Standard Floss Physical RTA plugin]: https://github.com/Daniel-KM/StandardFlossPhysicalRTAPlugin
[Primo]: http://www.exlibrisgroup.com/category/PrimoOverview
[Ex Libris]: http://exlibrisgroup.com
[ILS-DI]: http://diglib.org/architectures/ilsdi/DLF_ILS_Discovery_1.1.pdf
[Digital Library Federation]: http://diglib.org
[Koha]: https://koha-community.org
[1]: http://www.exlibrisgroup.com/default.asp?catid={6953052C-F108-4FB6-A5D4-F13C89CEE560}&details_type=1&itemid={D8EE06B2-3DC1-4868-9619-69F77217A9EE}
[RTA page]: https://developers.exlibrisgroup.com/primo/integrations/frontend/rta
[plugin issues]: https://github.com/Daniel-KM/StandardFlossPhysicalRTAPlugin/Issues
[CeCILL-B]: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html
[Daniel-KM]: http://github.com/Daniel-KM "Daniel Berthereau"
