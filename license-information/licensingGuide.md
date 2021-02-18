# License Guide
## Table of Contents
1. Introduction
2. AlarmBuddy's License
3. Best Practices for Third-Party Materials
4. License Information

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

1. When including third-party materials that are covered by a license enumerated in the `License Information` section of this document (hereafter "Enumerated License"), developers shall follow all of the requirements that are listed for that license. If these requirements include a prohibition on certain uses of the third-party materials, developers shall follow those prohibitions.
2. Developers should only use materials that are covered under an Enumerated License.
3. In extreme cases where a developer feels that they absolutely must use third-party material not covered under an Enumerated License, that developer shall coordinate with the licensing subteam member in their development group and their manager to determine whether the material's license is compatible with the AlarmBuddy project.
	* If the license is found to be compatible with the AlarmBuddy project, the licensing team shall add it to the list of Enumerated Licenses in this document.
	* If the license is found to be incompatible, the developer shall not include the third-party material in question.
4. Whenever a developer adds licensed third-party materials to the project, that developer shall update the `LICENSE-3RD-PARTY.txt` document in the root of this repository with the following information:
	* The title of the third-party material in question
	* The author's name and the year of copyright
	* A link to a source version of the material (when possible)
	* The full text of the license agreement

Multiple third-party items licensed under the same terms may be grouped together in the `LICENSE-3RD-PARTY.txt` document to avoid redundant inclusion of the same license text.

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

## Boost 1.0
Materials under this license may be freely used by developers. If the materials used are source code, developers shall retain the license text and copyright notice in any redistributions or derivative works.

## BSD 3-Clause
Materials under this license may be freely used by developers under the following conditions:
* If the materials used are source code files, developers shall retain the original copyright notice in the copied files
* If the materials used are binaries (i.e. libraries), developers shall include the original copyright notice in the project documentation


## Creative Commons BY (CC BY)
Developers may use materials licensed under CC BY, provided that they properly credit the original author. This credit shall be plainly visible to users and shall include the title of the original work, its author, and the original license of the work. Where possible, this attribution statement should link to the material's source, the author's online platform, and the text of the license. Developers may modify the source material. If the material is modified by a developer, they shall clearly indicate that the work has been modified in the author credit.

Some versions of CC BY include additional license requirements which expand upon the above:
* Share-alike `SA`: Derivative works shall be licensed under the same terms as the original material. If developers modify an original work under this license, they shall license this derivative work under the same terms as the original.
* Non-commercial `NC`: The work shall not be used in a commercial project. AlarmBuddy is not a commercial project, so developers may use works with this license requirement.
* No derivatives `ND`: The original material shall be used as-is, without modification. Developers may use these works, but shall not modify them.

CC BY licenses may contain zero or more of these additional requirements. For example, a work licensed under CC BY-NC-ND requires attribution, _and_ prohibits commercial use, _and_ prohibits modification of the original work. Developers shall follow all applicable license requirements for the materials they are including.

## Creative Commons Zero (CC0)
This license effectively dedicates the licensed work to the public domain, with no restrictions on subsequent use. Developers may use these materials for any purpose. While there is no explicit requirement to credit the original author, developers should still strive to do so.

## GNU Lesser GPL 2.1
When using this license you must include the original code, copyright, license, source, changes and include a notice. You can use the code in the project if you follow those musts and you can use libraries that are licensed under this license.

## Mozilla Public License 2.0
This is a permissive license that allows users to use the code as long as they do not trademark it. Only the specific files that are used must be licensed under this license and the entire project can have a different license. When using this license you must include the copyright, the original, the license and disclose the source of the code.

## Eclipse Public License 1.0
Materials under this license must include the original copyright notice in all uses of the work, disclose the source, if the software is in a commercial product you must compensate for damages in cases of lawsuits, include install and build instructions, and the location of where you can get an original copy.

## GNU Affero GPL 3
This license is designed for network software.  You can use modified versions as long as you track your changes, disclose source, include install instructions, and include the original license.  Derivatives must be licensed under GNU Affero GPL 3.

## GNU GPL 2.0
This is the version before GPL 3.0 where you are allowed to modify and distribute changes as long as you record the source file changes, if so you must license the code under GPL 2.0.  You are also required to state how to get the original copy and disclose your source code and include the license of the modified section.

## Unlicensed
DO NOT USE UNLICENSED CODE. The code is the property of the coder and you cannot use it without explicit permission of the owner in any way.

## The Unlicense
This license dedicates the work to the public domain. Developers may use this work freely for any purpose. Developers should try to give credit to the original author when possible.

## Apache 2.0
You can use any Apache License 2.0 licensed software in your commercial products for free. However, you must not name your product in a way that it looks like an endorsement from Apache. You must not use any Apache marks anywhere in your product or its documentation.

## GNU GPL 3.0
Software under the GPL may be run for all purposes, including commercial purposes and even as a tool for creating proprietary software, such as when using GPL-licensed compilers. GPL 3.0 and MIT are both permissive and universal and can be combined.

## BSD 2-Clause
The BSD 2-clause license allows you almost unlimited freedom with the software so long as you include the BSD copyright notice in it (found in Fulltext). Redistributions must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

## MIT
The MIT license is permissive and straightforward. It allows others to do whatever they want to the software if they include the original copyright and license notice in their copy of the software.