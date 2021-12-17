// swift-tools-version:5.5
import PackageDescription

let packageVerson = "v0.0.2"
let packageBinaryChecksum = "81b1858d36997dd33ae6de540edf739693815939bdf309801722a882d7223188"

let packageName = "FaithlifeOAuth"
let packageDownloadBaseUrl = "https://github.com/Faithlife/FaithlifeOAuth-Mobile/releases/download"
let packageBinaryZipName = "\(packageName).xcframework.zip"
let packageBinaryUrl = "\(packageDownloadBaseUrl)/\(packageVerson)/\(packageBinaryZipName)"

let package = Package(
	name: packageName,
	platforms: [
		.iOS(.v13),
	],
	products: [
		.library(name: "FaithlifeOAuth", targets: ["FaithlifeOAuth"]),
	],
	targets: [
		.binaryTarget(
			name: packageName,
			url: packageBinaryUrl,
			checksum: packageBinaryChecksum
		),
	]
)
