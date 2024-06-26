# Makefile for the database actor library.
#
# @Authors: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2008 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

ME =		ptolemy/actor/lib/database

DIRS = demo

# Root of the Java directory
ROOT =		../../../..

# ORACLE_OJDBC_JAR is set to the absolute path to ojdbc6.jar by configure
# MYSQL_JAR is set to the absolute path of the mysql jar file by configure 
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(ORACLE_OJDBC_JAR)$(CLASSPATHSEPARATOR)$(MYSQL_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	database
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	ArrayOfRecordsExporter.java \
	ArrayOfRecordsRecorder.java \
	DatabaseInsert.java \
	DatabaseManager.java \
	DatabaseQuery.java \
	DatabaseSelect.java \
	SQLStatement.java 


OTHER_FILES_TO_BE_JARED = \
	database.xml \
	DatabaseManagerIcon.xml \
	mysql-copyright.htm \

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo here, we don't want make sources to run make demo
MISC_FILES = \
	mysql-connector-java-8.0.26.jar

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = demo

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
