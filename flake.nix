{
  description = "Scala 3 flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }: flake-utils.lib.eachDefaultSystem (system:
    let
      pkgs = import nixpkgs { inherit system; };
    in
    {
      devShells.default = pkgs.mkShell {
        nativeBuildInputs = [
          pkgs.metals
          pkgs.ammonite
          pkgs.bloop
          pkgs.coursier
          pkgs.jdk11
          pkgs.jdk17
          pkgs.mill
          pkgs.sbt
          pkgs.scala-cli
          pkgs.scala_3
          pkgs.scalafmt
          pkgs.rlwrap
        ];
        shellHook = ''
          export JAVA_HOME=${pkgs.jdk11}/lib/openjdk
          export PATH=${pkgs.jdk11}/bin:$PATH
          export METALS_JAVA_HOME=${pkgs.jdk11}/lib/openjdk
          export DIRENV_LOG_FORMAT=
        '';
      };
    });
}
