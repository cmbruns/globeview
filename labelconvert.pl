#!/usr/local/bin/perl -w
use strict;

my $label_name = "?";
my $latitude = "?";
my $longitude = "?";
my $min_res = "?";
my $max_res = "?";

while (<>) {
  # // *** One city per continent resolution ***
  # l = new SiteLabel("London", d2r * 0.16, d2r * 51.5, labelColors);
  # l.setResolution(0.015, 0.060, 10.0, 20.0);
  # siteLabels.addElement(l);

  if ($_ =~ /new SiteLabel/) {
    die $_ unless $_ =~ /^\s+l = new SiteLabel\(\"([^\"]+)\", d2r \* ([-+0-9\.]+), d2r \* ([-+0-9\.]+), labelColors\);\s*$/;
    $label_name = $1;
    $latitude = $2;
    $longitude = $3;
  }

  if ($_ =~ /setResolution/) {
    die unless $_ =~ /^\s*l.setResolution\(([-+0-9\.]+),\s*([-+0-9\.]+),\s*([-+0-9\.]+),\s*([-+0-9\.]+)\);\s*$/;
    $min_res = $1;
    $max_res = $4;

    print "$label_name\t$latitude\t$longitude\t$min_res\t$max_res\n";
  }
}

