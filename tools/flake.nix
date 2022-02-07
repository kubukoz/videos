{
  outputs = { nixpkgs, ... }:
    let
      system = "aarch64-darwin";
      pkgs_x86 = import nixpkgs { localSystem = "x86_64-darwin"; };
      arm-overrides = _: _: { inherit (pkgs_x86) scala-cli; };

      pkgs = import nixpkgs {
        inherit system;
        overlays = [ arm-overrides ];
      };
    in
    {
      devShell.${system} = pkgs.mkShell { buildInputs = [ pkgs.scala-cli ]; };
    };
}
