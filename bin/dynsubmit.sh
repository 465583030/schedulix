#!/bin/sh
#
# $Id: dynsubmit.sh,v 1.2.6.2 2013/03/26 16:24:50 dieter Exp $
#
# Copyright (C) 2011 independIT Integrative Technologies GmbH
#
if [ -z "$BICSUITECONFIG" ]; then
        BICSUITECONFIG=$BICSUITEHOME/etc
fi

. $BICSUITECONFIG/java.conf || exit 1

cd $BICSUITEHOME/bin
$BICSUITEJAVA_TL -cp "$BICSUITECLASSPATH" de.independit.scheduler.demo.SDMSsubmitThreads "$@"
exit $?
