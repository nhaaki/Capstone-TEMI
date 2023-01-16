var bookname =  readLine("Book name: ");
var bookshelf =  readLine("Book shelf num: ");
var booklevel =  readLine("Book level: ");
var bookcallnum =  readLine("Book call number: ");


var url = [];
url.push(
  "http://temibot.com/level/level=",
  booklevel,
  "&shelfno=",
  bookshelf,
  "&bookname=",
  encodeURIComponent(bookname),
  "&bookid=",
  encodeURIComponent(bookcallnum)
);
var finalstring = url.join("");

println(finalstring)