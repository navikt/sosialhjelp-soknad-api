var fs = require("fs");
var path = require("path");
var readlineSync = require('readline-sync');

if (process.argv.length < 3) {
	console.log("Feil! Mangler språkfil.");
	console.log("Eksempel:");
	console.log(" > node generer.js ..\\..\\soknadsosialhjelp\\web\\src\\frontend" +
		"\\scripts\\mock_data\\soknadsosialhjelp_nb_NO.properties");
	process.exit(0);
}

var lesFilTilArray = function () {
	var fileContent = fs.readFileSync(fileName, "utf8");
	var array = fileContent.split("\n");
	var output = [];
	for (i in array) {
		var line = array[ i ];
		var splitChar = line.indexOf("=");
		var key = line.substring(0, splitChar);
		var val = line.substring(splitChar + 1, line.length);
		if (val) {
			output.push([ key, val.replace("\r", "") ]);
		}
	}
	return output;
};

var addedFiles = [];
var diffFiles = [];

var skrivTekstfil = function (tekstKey, tekst) {
	var dir = [ "..", "src", "main", "tekster", "soknadsosialhjelp" ];
	dir.push(tekstKey + "_nb_NO.txt");
	var outfile = dir.join(path.sep);
	if (fs.existsSync(outfile)) {
		var fileContent = fs.readFileSync(outfile, "utf8");
		var array = fileContent.split("\n");
		if (tekst.trim() !== array[ 0 ].trim()) {
			console.log("Ulik tekst i filen: " + outfile);
			console.log("  frontend : " + tekst.trim());
			console.log("  stash    : " + array[ 0 ].trim());
			if (readlineSync.keyInYN('Skrive over filen fra frontend til stash?')) {
				addedFiles.push(outfile);
				console.log(" Overskriver: " + outfile + " : " + tekst);
				fs.writeFileSync(outfile, tekst);
			} else {
				console.log('Hopper over tekstfil...');
				diffFiles.push(outfile);
			}
		} else {
			addedFiles.push(outfile);
			console.log(" Oppretter: " + outfile + " : " + tekst);
			fs.writeFileSync(outfile, tekst);
		}
	} else {
		console.log(" Skriver: " + outfile + " : " + tekst);
		fs.writeFileSync(outfile, tekst);
	}
};

var filenameArg = process.argv[ 2 ];
var directories = filenameArg.split("\\");
var fileName = directories.join(path.sep);

if (!fs.existsSync(fileName)) {
	console.log("Feil! Finner ikke språkfil " + fileName);
	process.exit(0);
} else {
	console.log("Leser språkfil:" + fileName);
	var tekstData = lesFilTilArray();
	for (item in tekstData) {
		var tekstKey = tekstData[ item ][ 0 ];
		var tekst = tekstData[ item ][ 1 ];
		skrivTekstfil(tekstKey, tekst);
	}
	console.log("");
	console.log("Antall tektser i frontend   : " + tekstData.length);
	console.log("Antall tekster med diff     : " + diffFiles.length);
	console.log("Antall tekstfiler opprettet : " + addedFiles.length);
	process.exit(0);
}

