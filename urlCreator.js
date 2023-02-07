var bookname =  prompt("Book name: "); 
var bookshelf =  prompt("Book shelf num: ");
var booklevel =  prompt("Book level: ");
var bookcallnum =  prompt("Book call number: ");


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

console.log(finalstring)
