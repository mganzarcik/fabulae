Utils = {};

Utils.pcColor = {
  border: '#2B7CE9',
  background: '#D2E5FF',
  highlight: {
    border: '#2B7CE9',
    background: '#D2E5FF'
  }
};

Utils.npcColor = {
  border: '#4d2600',
  background: '#cc6600',
  highlight: {
    border: '#663200',
    background: '#e67300'
  }
};

Utils.greetingColor = {
  border: '#009900',
  background: '#99ff99',
  highlight: {
    border: '#00b300',
    background: '#b3ffb3'
  }
};

Utils.endColor = {
  border: '#4d0000',
  background: '#cc0000',
  highlight: {
    border: '#660000',
    background: '#e60000'
  }
};

Utils.wordwrap = function(str, width, spaceReplacer) {
  if (str.length>width) {
      var p=width
      for (;p>0 && str[p]!=' ';p--) {
      }
      if (p>0) {
          var left = str.substring(0, p);
          var right = str.substring(p+1);
          return left + spaceReplacer + Utils.wordwrap(right, width, spaceReplacer);
      }
  }
  return str;
}