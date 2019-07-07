class std.lang.System
import std.io.FileWriter
import std.io.BufferedWriter
import std.io.Reader
import std.io.Writer
#standardInputRef static() int native
#standardOutputRef static() int native
#standardErrorRef static() int native
in static Reader
out static BufferedWriter : BufferedWriter(FileWriter(standardOutputRef()) as Writer)
err static BufferedWriter : BufferedWriter(FileWriter(standardErrorRef()) as Writer)
//TODO flush out and err in finalizer
