'use strict';

var fs              = require('fs');
var mapfile         = "combatMapGrassland1_res.tmx";
var oldRows         = 512;
var oldCols         = 8;
var newRows         = 64;
var tilecount       = 4096;
var firstGid        = 1030;
var newCols = oldCols * (oldRows / newRows);

function readFile(file, callback) {
    fs.readFile(file, {encoding: 'utf8'}, function (err, data) {
        if(err) {
            throw err;
        }
        callback(data);
    });
}

function saveContentToFile(fileName, content, callback) {
    fs.writeFile(fileName, content, function(err) {
        if(err) {
            throw err;
        } else {
            if(typeof callback === 'function') {
                callback();
            }            
        }
    });
}

function getNewId(oldId, log) {
    var oldRow = Math.ceil((oldId+1) / oldCols);
    if (log) {
        console.log("oldRow: "+oldRow);
    }
    var oldCol = (oldId+1) % oldCols;
    if (oldCol === 0) {
        oldCol = oldCols;
    }
    if (log) {
        console.log("oldCol: "+oldCol);
    }
    var newRow = oldRow - (Math.ceil(oldRow / newRows) - 1) * newRows;
    if (log) {
        console.log("newRow: "+newRow);
    }
    var newCol = (Math.ceil(oldRow / newRows) - 1) * oldCols + oldCol;
    if (log) {
        console.log("newCol: "+newCol);
    }
    return ((newRow - 1) * newCols) + newCol - 1;
}

readFile(mapfile, function (dataOrig) {
    
    var dataNew = dataOrig.replace(/<tile (id|gid)="([0-9]*)"(\/?)>/g, function(match, p1, p2, p3) {
        var id = parseInt(p2);
        var newId = id;
        var offset = p1 === "gid" ? firstGid : 0;

        if (id < (tilecount+offset) && id >= offset) {
            if (p1 === "gid") {
                id -= firstGid;
            }
            newId = getNewId(id);
            if (p1 === "gid") {
                newId += firstGid;
            }
        }
           
        return "<tile "+p1+"=\""+newId+"\""+(p3 ? p3 : "")+">";
    });
    
    saveContentToFile(mapfile+"_edit", dataNew);
});