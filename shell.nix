{ pkgs ? import <nixpkgs> {}, ... }:

let
jdk = pkgs.jdk22;
gradle= pkgs.gradle.override { java = jdk; };
in
pkgs.mkShell
{
  packages = with pkgs; [jdk gradle nodejs_22];
}

