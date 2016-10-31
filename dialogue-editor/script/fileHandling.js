function setNodeText(node, text, dataset) {
  var f_text = text || "";
  var dots = f_text.length > 99;
  var label = Utils.wordwrap(f_text.substring(0, 100), 40, "\n")+ (dots ? "..." : "");
  var title = Utils.wordwrap(f_text, 40, "<br>");

  if (dataset) {
    dataset.update({
      id: node.id,
      f_text: f_text,
      label: label,
      title: title
    })
  } else {
    node.f_text = f_text;
    node.label = label;
    node.title = title;
  }
}

function getXmlExport(dialogueId, data) {
  var nodes = [];
  data.nodes.forEach(function(node) {
    var nodeData = {
      id: node.f_id,
      type: node.f_type,
      text: node.f_text,
      condition: node.f_condition,
      action: node.f_action,
      endOfConversation: node.f_dialogueEnd,
      connections: []
    };

    data.edges.forEach(function(edge) {
      if (edge.from === node.id) {
        nodeData.connections.push(data.nodes.get(edge.to).f_id);
      }
    });
    nodes.push(nodeData);
  });

  var output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
               "<!DOCTYPE document SYSTEM \"..\\..\\..\\..\\..\\core\\dtd\\dialogue.dtd\">\n"+
               "<dialogue>\n";
  for (var i in nodes) {
    var node = nodes[i];
    output += "    <"+node.type+" id=\""+node.id+"\""+(node.endOfConversation ? " endOfConversation=\"true\"" : "")+">\n";
    if (node.condition) {
      output += "        <condition>\n"
      output += "            "+node.condition+"\n";
      output += "        </condition>\n"
    }
    if (node.action) {
      output += "        <action>\n"
      output += "            "+node.action+"\n";
      output += "        </action>\n"
    }
    output += "        <text>\n"
    output += "            "+dialogueId+".json#"+node.id+"\n";
    output += "        </text>\n"
    var connElement = node.type === "pcTalk" ? "npcTalk" : "pcTalk";
    for (var j in node.connections) {
      output += "        <"+connElement+" id=\""+node.connections[j]+"\" />\n";
    }
    output += "    </"+node.type+">\n";
  }

  output += "</dialogue>";
  return output;
}

function handleExport (evt) {
  var dialogueId = document.getElementById('dialogue-id').value;
  if (!dialogueId || !window.data || !window.data.nodes) {
    return;
  }
  var zip = new JSZip();
  var languageFile = {};

  window.data.nodes.forEach(function(node) {
    languageFile[node.f_id] = node.f_text;
  });

  zip
    .folder("dialogues")
      .file(dialogueId+".xml", getXmlExport(dialogueId, window.data));
  zip
      .folder("strings")
      .folder("en")
        .file(dialogueId+".json", JSON.stringify(languageFile, null, 4));

  var content = zip.generate({type : "blob"});
  window.saveAs(content, dialogueId+".zip");
}

function getLanguageFileKey(text) {
  var index = text.indexOf(".json#");
  if (index > 0) {
    return text.substring(index + 6);
  }
  return undefined;
}

function handleLanguageFileUpload (evt) {
  var files = evt.target.files; // FileList object

  var output = [];
  for (var i = 0, f; f = files[i]; i++) {
    var reader = new FileReader();

    reader.onload = (function(theFile) {
      return function(e) {
        window.languageFile = JSON.parse(e.target.result);

        if (window.data && window.data.nodes) {
          window.data.nodes.forEach(function(node) {
            var key = getLanguageFileKey(node.f_text);
            if (key && window.languageFile[key]) {
              setNodeText(node, window.languageFile[key], window.data.nodes);
            }
          });
        }
      }
    })(f);

    reader.readAsText(f);
  }
}

function handleDialogueFileUpload(evt) {
  var files = evt.target.files; // FileList object

  var output = [];
  for (var i = 0, f; f = files[i]; i++) {
    var reader = new FileReader();
    document.getElementById('dialogue-id').value = f.name.replace(".xml", "");
    reader.onload = (function(theFile) {
      return function(e) {
        var dialogueJson = $.xml2json(e.target.result, false, ["condition", "action"]);
        var nodes = [];
        var edges = [];

        function createEdge(fromId, toId, type) {
          return {
            from: fromId,
            to: toId.indexOf(type) === 0 ? toId : type+toId,
            arrows: {
              to: true
            },
            smooth: {
              enabled: false
            }
          }   ;
        }

        function createNodeAndEdges(item) {
          var node = {};
          var Utils = window.Utils;
          node.f_type = type;
          node.f_condition = item.condition;
          node.f_action = item.action;
          node.f_dialogueEnd = item.endOfConversation === "true";
          var id = item.id.indexOf(type) === 0 ? item.id : type+item.id;
          node.f_id = id;
          node.id = id;
          var text = item.text ? (item.text[0] || (item.text || "")) : "";
          var key = getLanguageFileKey(text);
          if (window.languageFile && key && window.languageFile[key]) { 
            text = window.languageFile[key];
          }
          setNodeText(node, text);

          var isGreeting = node.f_type === "greeting";
          var isNPC = node.f_type === "npcTalk";

          node.shape = 'box';
          node.color = isGreeting ? Utils.greetingColor : (isNPC ? Utils.npcColor : (node.f_dialogueEnd ? Utils.endColor : Utils.pcColor));
          node.font = {size:20};
          node.physics = true;
          
          if (item.pcTalk) {
            if (item.pcTalk.id) {
              edges.push(createEdge(node.id, item.pcTalk.id, "pcTalk"));
            } else {
              for (var k in item.pcTalk) {
                edges.push(createEdge(node.id, item.pcTalk[k].id, "pcTalk"));
              }
            }
          }

          if (item.npcTalk) {
            if (item.npcTalk.id) {
              edges.push(createEdge(node.id, item.npcTalk.id, "npcTalk"));
            } else {
              for (var k in item.npcTalk) {
                edges.push(createEdge(node.id, item.npcTalk[k].id, "npcTalk"));
              }
            }
          }

          nodes.push(node);
        }

        for (var type in dialogueJson) {
          if (!dialogueJson.hasOwnProperty(type)) {
            continue;
          }

          var items = dialogueJson[type];

          if (items.id) {
            createNodeAndEdges(items);
          } else {
            for (var j in items) {
              createNodeAndEdges(items[j]);
            }
          }

          window.nodeCount = nodes.length + 1;

          window.data = {
            nodes: new vis.DataSet(nodes),
            edges: new vis.DataSet(edges)
          }
          window.draw();
        }
      };
    })(f);

    reader.readAsText(f);
  }
}