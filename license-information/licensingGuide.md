# License Guide
## Table of Contents
1. Introduction
2. AlarmBuddy's License
3. Best Practices for Third-Party Materials
4. Filling out `LICENSE-3RD-PARTY.txt`
5. License Information

# Introduction
This document outlines licensing information for the AlarmBuddy software project. Developers of the project should be familiar with and follow the best practices outlined within, and use this document as reference material when using third-party licensed materials or software. Questions about this document should be directed to the licensing expert on each development team:
* Amazon: Jake Lauridsen
* Android: Chase Rapp
* Database: Jacob Jensen
* Web Development: Alex Yurevich

# AlarmBuddy's License
AlarmBuddy is licensed under the MIT Software License. The full text of this license can be found in the `LICENSE.txt` file in the repository's root directory. This license was chosen because it mixes well with other open-source licenses, explicitly releases the developers of AlarmBuddy from liability, and is very permissive in terms of derivative uses.

# Best Practices for Third-Party Materials
Developers shall follow these best practices when using any third-party copyrighted materials as part of the AlarmBuddy project. Materials covered by these rules include, but are not limited to, software libraries, source code, and media files.

1. When including third-party materials that are covered by a license enumerated in the `License Information` section of this document (hereafter "Enumerated License"), developers shall follow all of the requirements that are listed for that license. If these requirements include a prohibition on certain uses of the third-party materials, developers shall comply with those prohibitions.

2. Developers should only use materials that are covered under an Enumerated License.

3. In extreme cases where a developer feels that they absolutely must use third-party material not covered under an Enumerated License, that developer shall coordinate with the licensing subteam member in their development group and their manager to determine whether the material's license is compatible with the AlarmBuddy project.
	* If the license is found to be compatible with the AlarmBuddy project, the licensing team shall add it to the list of Enumerated Licenses in this document
	* If the license is found to be incompatible, the developer shall not include the third-party material in question
4. When including third-party materials in the AlarmBuddy project, developers shall update the `LICENSE-3RD-PARTY.txt` update in the root of this repository as described in the `Filling Out LICENSE-3RD-PARTY.txt` section of this document. This update shall be made for each new third-party item being added to the AlarmBuddy project.
	* For some of the more permissive Enumerated Licenses, the requirement to update `LICENSE-3RD-PARTY.txt` is explicitly waived in the `License Information` section. Developers are not required to update the `LICENSE-3RD-PARTY` file when using these licenses. The requirement stands in cases where this waiver is not present.

# Filling out `LICENSE-3RD-PARTY.txt`
Whenever a developer updates the `LICENSE-3RD-PARTY.txt` file, they shall include the following information:
* The title of the third-party material in question
* The author's name and the year of copyright
* A link to a source version of the material (when possible)
* The full text of the license agreement

Some other things to note:
* Developers shall update `LICENSE-3RD-PARTY.txt` _**in addition**_ to complying with the individual requirements for the Enumerated License being used. One does not substitute for the other, and in some cases both steps are required under the license terms.
* Multiple third-party items licensed under the same terms should be grouped together in the `LICENSE-3RD-PARTY.txt` document to avoid redundant inclusion of the same license text.

Example:
```
-------------------------------------------------------------------------------
MIT License:
	AlarmBuddy
		Copyright 2021 Students of University of St. Thomas CISC 480-D01, Spring 2021
		https://github.com/RileyLipinski/AlarmBuddyClock
	Some Other Software Library
		Copyright 1993 John Doe
		https://someothersoftwarelibrary.org/download
	<etc.>

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the “Software”), to deal in
the Software without restriction, including without limitation...
-------------------------------------------------------------------------------
GNU General Public License v2.0
	OtherProject
		Copyright 1970 Jane Doe
	...
-------------------------------------------------------------------------------
```
# License Information

## Apache 2.0
Developers may use materials under this license for any purpose. Developers shall retain copyright notices in licensed third-party files. If developers change the materials they are using, they shall place a notice within the file stating that changes have been made from the original. If the source material contains a `NOTICE` file, that file shall be included along with the licensed materials.

## Boost 1.0
There are no additional requirements for using software under this license. Developers shall continue to follow all requirements from the the above `Best Practices` section.

## BSD 2-Clause and BSD 3-Clause
Materials under these licenses may be freely used by developers under the following conditions:
* If the materials used are source code files, developers shall retain the original copyright notice in the copied files
* If the materials used are binaries (i.e. libraries), developers shall include the original copyright notice in the project documentation

## Creative Commons BY (CC BY)
Developers may use materials licensed under CC BY, provided that they properly credit the original author. This credit shall be plainly visible to users and shall include the title of the original work, its author, and the original license of the work. Where possible, this attribution statement should link to the material's source, the author's online platform, and the text of the license. Developers may modify the source material. If the material is modified by a developer, they shall clearly indicate that the work has been modified in the author credit.

Some versions of CC BY include additional license requirements which expand upon the above:
* Share-alike `SA`: Derivative works shall be licensed under the same terms as the original material. If developers modify an original work under this license, they shall license this derivative work under the same terms as the original.
* Non-commercial `NC`: The work shall not be used in a commercial project. AlarmBuddy is not a commercial project, so developers may use works with this license requirement.
* No derivatives `ND`: The original material shall be used as-is, without modification. Developers may use these works, but shall not modify them.

CC BY licenses may contain zero or more of these additional requirements. For example, a work licensed under CC BY-NC-ND requires attribution, _and_ prohibits commercial use, _and_ prohibits modification of the original work. Developers shall follow all applicable license requirements for the materials they are including.
* The requirement to fill out `LICENSE-3RD-PARTY.txt` is _**waived**_ for materials licensed under CC BY or any variant thereof

## Creative Commons Zero (CC0)
This license effectively dedicates the licensed work to the public domain, with no restrictions on subsequent use. Developers may use these materials for any purpose. While there is no explicit requirement to credit the original author, developers should still strive to do so.
* The requirement to fill out `LICENSE-3RD-PARTY.txt` is _**waived**_ for materials licensed under CC0

## Eclipse Public License 1.0
Developers shall provide installation instructions for these materials or any derivative works based upon them.

## GNU Affero GPL 3
Developers shall follow all rules for the `GNU GPL 3.0` (see below). In addition to those requirements, developers shall not create derivative works from these materials, regardless of the ultimate use or purpose of the derivative. For example, it is permissible to use a GPL3 Affero-licensed operating system to run AlarmBuddy's servers, but it is not permissible to modify that operating system.

## GNU GPL 3.0
Developers shall not distribute materials developed under this license, nor derivative works based upon them. Developers shall not include these materials within the source code of the AlarmBuddy project. Developers may use these materials on the back-end of the project (i.e. the operating system of an AlarmBuddy web server). If a developer is not sure whether a use is permissible, they should reach out to the licensing team for clarification.

## GNU Lesser GPL 2.1
This license generally applies to software libraries. Developers may use these libraries as a part of the AlarmBuddy project, but they shall not create derivative works from them.

## GNU GPL 2.0
Developers may use these materials for any purpose. Developers shall not make derivative works from these materials.

## MIT
There are no additional requirements for using software under this license. Developers shall continue to follow all requirements from the the above `Best Practices` section.

## Mozilla Public License 2.0
Developers may include these materials for any purpose. Developers shall not make derivative works from these materials.

## The Unlicense
This license dedicates the work to the public domain. Developers may use this work freely for any purpose. Developers should try to give credit to the original author when possible.
* The requirement to fill out `LICENSE-3RD-PARTY.txt` is _**waived**_ for materials licensed under the Unlicense