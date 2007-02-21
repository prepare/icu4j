#!/usr/bin/perl

#/**
# *******************************************************************************
# * Copyright (C) 2006, International Business Machines Corporation and         *
# * others. All Rights Reserved.                                                *
# *******************************************************************************
# */
usage() unless defined ($ENV{'ICU4J_ROOT'});
use lib "$ENV{'ICU4J_ROOT'}/src/com/ibm/icu/dev/test/perf/perldriver";
use PerfFramework4j;

my $ICU4J_ROOT = $ENV{'ICU4J_ROOT'};
$TEST_DATA="$ICU4J_ROOT/src/com/ibm/icu/dev/test/perf/data/collation";

#----------------------------------------------------------------
sub usage{
    print "Please define ICU4J_ROOT env variable.\n";
    exit -1;
}

#---------------------------------------------------------------------
# Test class
my $TESTCLASS = "com.ibm.icu.dev.test.perf.ResourceBundlePerf"; 

mkdir "results_ICU4J";

my $options = {
	       "title"=>"ResourceBunlde performance test",
	       "headers"=>"Java ICU",
	       "operationIs"=>"various",
	       "timePerOperationIs"=>"Time per each fetch",
	       "passes"=>"10",
	       "time"=>"60",
	       #"outputType"=>"HTML",
	       "dataDir"=>$TEST_DATA,
	       "outputDir"=>"results_ICU4J"
	      };

# programs

my $cmd = "java -cp \"$ICU4J_ROOT/classes\" $TESTCLASS";

my $dataFiles = "";

my $tests = { 
               "Empty array",           ["$cmd TestEmptyArrayJava",                 "$cmd TestEmptyArrayICU"],
               "Empty Explicit String", ["$cmd TestEmptyExplicitStringJava",        "$cmd TestEmptyExplicitStringICU"],
               "Empty String",          ["$cmd TestEmptyStringJava",                "$cmd TestEmptyStringICU"],
               "Get 123",               ["$cmd TestGet123Java",                     "$cmd TestGet123ICU"],
               "Get Binary Test",       ["$cmd TestGetBinaryTestJava",              "$cmd TestGetBinaryTestICU"],
               "Get Empty Binary",      ["$cmd TestGetEmptyBinaryJava",             "$cmd TestGetBinaryTestICU"],
               "Get Empty Menu",        ["$cmd TestGetEmptyMenuJava",               "$cmd TestGetEmptyMenuICU"],
               "Get Empty Int",         ["$cmd TestGetEmptyIntJava",                "$cmd TestGetEmptyIntICU"],
               "Get Empty Int Array",   ["$cmd TestGetEmptyIntegerArrayJava",       "$cmd TestGetEmptyIntegerArrayICU"],
               "Get Int Array",         ["$cmd TestGetIntegerArrayJava",            "$cmd TestGetIntegerArrayICU"],
               "Get Menu",              ["$cmd TestGetMenuJava",                    "$cmd TestGetMenuICU"],
               "Get Minus One",         ["$cmd TestGetMinusOneJava",                "$cmd TestGetMinusOneICU"],
               "Get Minus One Uint",    ["$cmd TestGetMinusOneUintJava",            "$cmd TestGetMinusOneUintICU"],
               "Get One",               ["$cmd TestGetOneJava",                     "$cmd TestGetOneICU"],
               "Get Plus One",          ["$cmd TestGetPlusOneJava",                 "$cmd TestGetPlusOneICU"],
               "Construction",          ["$cmd TestResourceBundleConstructionJava", "$cmd TestResourceBundleConstructionICU"],
               "Zero Test",             ["$cmd TestZeroTestJava",                   "$cmd TestZeroTestICU"]
            };


runTests($options, $tests, $dataFiles);


