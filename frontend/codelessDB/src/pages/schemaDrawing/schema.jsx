import {
  Background,
  Controls,
  MiniMap,
  ReactFlow,
  ReactFlowProvider,
  useReactFlow,
  useViewport
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useNotification } from "../../components/NotificationContext";
import { uploadToCloudinary } from "../../components/uploadImage.js";
import CodeEditor from "./code-editor/CodeEditor.jsx";
import Cursor from "./collab/Cursor.jsx";
import applyRelationLogic from "./connectingLogic/ConnectingLogic";
import {
  generateSQLFromBackend,
  updateDiagram,
  fetchDiagramMetadata,
  fetchDiagramSnapshot
} from "./fetch.js";

import { validateSchema } from "./generate/CheckCorrectness";
import { convertToJSON } from "./generate/JsonConverter";
import { edgeTypes, nodeTypes } from "./index";
import "./Schema.css";

// 1. IMPORT HTML-TO-IMAGE
import { toPng } from "html-to-image";
import Toolbar from "./ConnectionControls.jsx";

import * as Y from "yjs";
import ActiveUsers from "./collab/ActiveUsers.jsx";
import {
  CollaborationProvider,
  useCollaboration,
} from "./collab/CollaborationContext.jsx";

function uint8ArrayToBase64(bytes) {
  let binary = '';
  const len = bytes.byteLength;
  for (let i = 0; i < len; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return window.btoa(binary);
}

const SchemaContent = () => {
  const { roomId } = useParams();
  const { showSuccess, showError, showWarning } = useNotification();

  // 3. USE THE CONTEXT
  const {
    ydoc,
    nodes,
    edges,
    schemaName,
    updateSchemaName,
    cursors,
    updateCursor,
    onNodesChange,
    onEdgesChange,
    addNodeYjs,
    addEdgeYjs,
    updateNodeData,
    loadCompositeYjsData,
    undo,
    redo
  } = useCollaboration();

  const [selectedRelationType, setSelectedRelationType] = useState("1:N");
  const [isSqlPanelOpen, setIsSqlPanelOpen] = useState(false);
  const [generatedSql, setGeneratedSql] = useState("");
  const [isReadOnly, setIsReadOnly] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [reactFlowInstance, setReactFlowInstance] = useState(null);

  const { screenToFlowPosition } = useReactFlow();

  const onMouseMove = useCallback(
    (e) => {
      // Convert pixel coordinates (e.clientX) to World coordinates (Flow X)
      // This handles Zoom and Pan automatically.
      const position = screenToFlowPosition({
        x: e.clientX,
        y: e.clientY,
      });

      // Broadcast the World Position
      updateCursor(position.x, position.y);
    },
    [screenToFlowPosition, updateCursor]
  );

  const x = {
    "0": 1,
    "1": 4,
    "2": 236,
    "3": 243,
    "4": 171,
    "5": 164,
    "6": 4,
    "7": 0,
    "8": 33,
    "9": 1,
    "10": 4,
    "11": 109,
    "12": 101,
    "13": 116,
    "14": 97,
    "15": 4,
    "16": 110,
    "17": 97,
    "18": 109,
    "19": 101,
    "20": 2,
    "21": 40,
    "22": 1,
    "23": 5,
    "24": 110,
    "25": 111,
    "26": 100,
    "27": 101,
    "28": 115,
    "29": 15,
    "30": 49,
    "31": 95,
    "32": 49,
    "33": 55,
    "34": 54,
    "35": 54,
    "36": 50,
    "37": 52,
    "38": 53,
    "39": 54,
    "40": 51,
    "41": 51,
    "42": 49,
    "43": 53,
    "44": 49,
    "45": 1,
    "46": 118,
    "47": 4,
    "48": 2,
    "49": 105,
    "50": 100,
    "51": 119,
    "52": 15,
    "53": 49,
    "54": 95,
    "55": 49,
    "56": 55,
    "57": 54,
    "58": 54,
    "59": 50,
    "60": 52,
    "61": 53,
    "62": 54,
    "63": 51,
    "64": 51,
    "65": 49,
    "66": 53,
    "67": 49,
    "68": 4,
    "69": 116,
    "70": 121,
    "71": 112,
    "72": 101,
    "73": 119,
    "74": 11,
    "75": 68,
    "76": 101,
    "77": 102,
    "78": 117,
    "79": 108,
    "80": 116,
    "81": 45,
    "82": 78,
    "83": 111,
    "84": 100,
    "85": 101,
    "86": 4,
    "87": 100,
    "88": 97,
    "89": 116,
    "90": 97,
    "91": 118,
    "92": 2,
    "93": 9,
    "94": 116,
    "95": 97,
    "96": 98,
    "97": 108,
    "98": 101,
    "99": 78,
    "100": 97,
    "101": 109,
    "102": 101,
    "103": 119,
    "104": 8,
    "105": 69,
    "106": 110,
    "107": 116,
    "108": 105,
    "109": 116,
    "110": 121,
    "111": 95,
    "112": 49,
    "113": 7,
    "114": 99,
    "115": 111,
    "116": 108,
    "117": 117,
    "118": 109,
    "119": 110,
    "120": 115,
    "121": 117,
    "122": 1,
    "123": 118,
    "124": 4,
    "125": 2,
    "126": 105,
    "127": 100,
    "128": 119,
    "129": 21,
    "130": 97,
    "131": 116,
    "132": 116,
    "133": 114,
    "134": 49,
    "135": 95,
    "136": 49,
    "137": 95,
    "138": 49,
    "139": 55,
    "140": 54,
    "141": 54,
    "142": 50,
    "143": 52,
    "144": 53,
    "145": 54,
    "146": 51,
    "147": 51,
    "148": 49,
    "149": 53,
    "150": 49,
    "151": 4,
    "152": 110,
    "153": 97,
    "154": 109,
    "155": 101,
    "156": 119,
    "157": 2,
    "158": 105,
    "159": 100,
    "160": 8,
    "161": 100,
    "162": 97,
    "163": 116,
    "164": 97,
    "165": 84,
    "166": 121,
    "167": 112,
    "168": 101,
    "169": 119,
    "170": 3,
    "171": 73,
    "172": 78,
    "173": 84,
    "174": 11,
    "175": 99,
    "176": 111,
    "177": 110,
    "178": 115,
    "179": 116,
    "180": 114,
    "181": 97,
    "182": 105,
    "183": 110,
    "184": 116,
    "185": 115,
    "186": 118,
    "187": 1,
    "188": 11,
    "189": 80,
    "190": 82,
    "191": 73,
    "192": 77,
    "193": 65,
    "194": 82,
    "195": 89,
    "196": 95,
    "197": 75,
    "198": 69,
    "199": 89,
    "200": 120,
    "201": 8,
    "202": 112,
    "203": 111,
    "204": 115,
    "205": 105,
    "206": 116,
    "207": 105,
    "208": 111,
    "209": 110,
    "210": 118,
    "211": 2,
    "212": 1,
    "213": 120,
    "214": 123,
    "215": 64,
    "216": 114,
    "217": 41,
    "218": 0,
    "219": 51,
    "220": 113,
    "221": 25,
    "222": 151,
    "223": 1,
    "224": 121,
    "225": 123,
    "226": 64,
    "227": 116,
    "228": 165,
    "229": 234,
    "230": 226,
    "231": 59,
    "232": 119,
    "233": 30,
    "234": 129,
    "235": 236,
    "236": 243,
    "237": 171,
    "238": 164,
    "239": 4,
    "240": 1,
    "241": 1,
    "242": 136,
    "243": 236,
    "244": 243,
    "245": 171,
    "246": 164,
    "247": 4,
    "248": 3,
    "249": 1,
    "250": 119,
    "251": 13,
    "252": 116,
    "253": 101,
    "254": 115,
    "255": 116,
    "256": 105,
    "257": 110,
    "258": 103,
    "259": 32,
    "260": 115,
    "261": 104,
    "262": 97,
    "263": 114,
    "264": 101,
    "265": 27,
    "266": 236,
    "267": 243,
    "268": 171,
    "269": 164,
    "270": 4,
    "271": 2,
    "272": 0,
    "273": 2,
    "274": 3,
    "275": 1,
    "276": 248,
    "277": 51,
    "278": 12,
    "279": 1,
    "280": 156,
    "281": 251,
    "282": 182,
    "283": 157,
    "284": 13,
    "285": 129,
    "286": 252,
    "287": 182,
    "288": 157,
    "289": 13,
    "290": 136,
    "291": 156,
    "292": 251,
    "293": 182,
    "294": 157,
    "295": 13,
    "296": 128,
    "297": 153,
    "298": 178,
    "299": 212,
    "300": 170,
    "301": 13,
    "302": 1,
    "303": 247,
    "304": 153,
    "305": 178,
    "306": 212,
    "307": 170,
    "308": 13,
    "309": 13,
    "310": 244,
    "311": 154,
    "312": 178,
    "313": 212,
    "314": 170,
    "315": 13,
    "316": 101,
    "317": 243,
    "318": 155,
    "319": 178,
    "320": 212,
    "321": 170,
    "322": 13,
    "323": 116,
    "324": 233,
    "325": 156,
    "326": 178,
    "327": 212,
    "328": 170,
    "329": 13,
    "330": 110,
    "331": 231,
    "332": 157,
    "333": 178,
    "334": 212,
    "335": 170,
    "336": 13,
    "337": 32,
    "338": 243,
    "339": 158,
    "340": 178,
    "341": 212,
    "342": 170,
    "343": 13,
    "344": 104,
    "345": 225,
    "346": 159,
    "347": 178,
    "348": 212,
    "349": 170,
    "350": 13,
    "351": 114,
    "352": 229,
    "353": 160,
    "354": 178,
    "355": 212,
    "356": 170,
    "357": 13,
    "358": 18,
    "359": 240,
    "360": 197,
    "361": 177,
    "362": 192,
    "363": 183,
    "364": 13,
    "365": 1,
    "366": 228,
    "367": 25,
    "368": 11,
    "369": 1,
    "370": 156,
    "371": 251,
    "372": 182,
    "373": 157,
    "374": 13,
    "375": 129,
    "376": 252,
    "377": 182,
    "378": 157,
    "379": 13,
    "380": 136,
    "381": 156,
    "382": 251,
    "383": 182,
    "384": 157,
    "385": 13,
    "386": 128,
    "387": 153,
    "388": 178,
    "389": 212,
    "390": 170,
    "391": 13,
    "392": 1,
    "393": 247,
    "394": 153,
    "395": 178,
    "396": 212,
    "397": 170,
    "398": 13,
    "399": 13,
    "400": 244,
    "401": 154,
    "402": 178,
    "403": 212,
    "404": 170,
    "405": 13,
    "406": 101,
    "407": 243,
    "408": 155,
    "409": 178,
    "410": 212,
    "411": 170,
    "412": 13,
    "413": 116,
    "414": 233,
    "415": 156,
    "416": 178,
    "417": 212,
    "418": 170,
    "419": 13,
    "420": 110,
    "421": 231,
    "422": 157,
    "423": 178,
    "424": 212,
    "425": 170,
    "426": 13,
    "427": 32,
    "428": 243,
    "429": 158,
    "430": 178,
    "431": 212,
    "432": 170,
    "433": 13,
    "434": 104,
    "435": 225,
    "436": 159,
    "437": 178,
    "438": 212,
    "439": 170,
    "440": 13,
    "441": 114,
    "442": 229,
    "443": 160,
    "444": 178,
    "445": 212,
    "446": 170,
    "447": 13,
    "448": 1,
    "449": 190,
    "450": 25,
    "451": 10,
    "452": 1,
    "453": 150,
    "454": 154,
    "455": 128,
    "456": 246,
    "457": 11,
    "458": 129,
    "459": 155,
    "460": 128,
    "461": 246,
    "462": 11,
    "463": 136,
    "464": 150,
    "465": 154,
    "466": 128,
    "467": 246,
    "468": 11,
    "469": 128,
    "470": 178,
    "471": 154,
    "472": 246,
    "473": 129,
    "474": 12,
    "475": 1,
    "476": 247,
    "477": 178,
    "478": 154,
    "479": 246,
    "480": 129,
    "481": 12,
    "482": 13,
    "483": 244,
    "484": 179,
    "485": 154,
    "486": 246,
    "487": 129,
    "488": 12,
    "489": 101,
    "490": 243,
    "491": 180,
    "492": 154,
    "493": 246,
    "494": 129,
    "495": 12,
    "496": 116,
    "497": 233,
    "498": 181,
    "499": 154,
    "500": 246,
    "501": 129,
    "502": 12,
    "503": 110,
    "504": 231,
    "505": 182,
    "506": 154,
    "507": 246,
    "508": 129,
    "509": 12,
    "510": 32,
    "511": 243,
    "512": 183,
    "513": 154,
    "514": 246,
    "515": 129,
    "516": 12,
    "517": 104,
    "518": 225,
    "519": 184,
    "520": 154,
    "521": 246,
    "522": 129,
    "523": 12,
    "524": 114,
    "525": 152,
    "526": 25,
    "527": 9,
    "528": 1,
    "529": 139,
    "530": 244,
    "531": 174,
    "532": 226,
    "533": 10,
    "534": 128,
    "535": 245,
    "536": 174,
    "537": 226,
    "538": 10,
    "539": 161,
    "540": 237,
    "541": 226,
    "542": 216,
    "543": 147,
    "544": 8,
    "545": 136,
    "546": 227,
    "547": 145,
    "548": 187,
    "549": 158,
    "550": 8,
    "551": 55,
    "552": 129,
    "553": 228,
    "554": 145,
    "555": 187,
    "556": 158,
    "557": 8,
    "558": 201,
    "559": 208,
    "560": 176,
    "561": 143,
    "562": 10,
    "563": 128,
    "564": 181,
    "565": 194,
    "566": 202,
    "567": 168,
    "568": 8,
    "569": 161,
    "570": 144,
    "571": 250,
    "572": 238,
    "573": 131,
    "574": 7,
    "575": 129,
    "576": 198,
    "577": 188,
    "578": 185,
    "579": 172,
    "580": 15,
    "581": 2,
    "582": 130,
    "583": 199,
    "584": 188,
    "585": 185,
    "586": 172,
    "587": 15,
    "588": 148,
    "589": 217,
    "590": 128,
    "591": 206,
    "592": 9,
    "593": 128,
    "594": 161,
    "595": 189,
    "596": 135,
    "597": 182,
    "598": 15,
    "599": 40,
    "600": 129,
    "601": 162,
    "602": 189,
    "603": 135,
    "604": 182,
    "605": 15,
    "606": 5,
    "607": 193,
    "608": 11,
    "609": 1,
    "610": 10,
    "611": 212,
    "612": 245,
    "613": 246,
    "614": 225,
    "615": 6,
    "616": 165,
    "617": 7,
    "618": 6,
    "619": 1,
    "620": 176,
    "621": 247,
    "622": 241,
    "623": 159,
    "624": 9,
    "625": 129,
    "626": 248,
    "627": 241,
    "628": 159,
    "629": 9,
    "630": 136,
    "631": 176,
    "632": 247,
    "633": 241,
    "634": 159,
    "635": 9,
    "636": 128,
    "637": 169,
    "638": 233,
    "639": 145,
    "640": 169,
    "641": 9,
    "642": 1,
    "643": 247,
    "644": 169,
    "645": 233,
    "646": 145,
    "647": 169,
    "648": 9,
    "649": 13,
    "650": 244,
    "651": 170,
    "652": 233,
    "653": 145,
    "654": 169,
    "655": 9,
    "656": 101,
    "657": 243,
    "658": 171,
    "659": 233,
    "660": 145,
    "661": 169,
    "662": 9,
    "663": 116,
    "664": 255,
    "665": 6,
    "666": 5,
    "667": 1,
    "668": 176,
    "669": 247,
    "670": 241,
    "671": 159,
    "672": 9,
    "673": 129,
    "674": 248,
    "675": 241,
    "676": 159,
    "677": 9,
    "678": 136,
    "679": 176,
    "680": 247,
    "681": 241,
    "682": 159,
    "683": 9,
    "684": 128,
    "685": 169,
    "686": 233,
    "687": 145,
    "688": 169,
    "689": 9,
    "690": 1,
    "691": 247,
    "692": 169,
    "693": 233,
    "694": 145,
    "695": 169,
    "696": 9,
    "697": 13,
    "698": 244,
    "699": 170,
    "700": 233,
    "701": 145,
    "702": 169,
    "703": 9,
    "704": 101,
    "705": 217,
    "706": 6,
    "707": 4,
    "708": 1,
    "709": 176,
    "710": 247,
    "711": 241,
    "712": 159,
    "713": 9,
    "714": 129,
    "715": 248,
    "716": 241,
    "717": 159,
    "718": 9,
    "719": 136,
    "720": 176,
    "721": 247,
    "722": 241,
    "723": 159,
    "724": 9,
    "725": 128,
    "726": 169,
    "727": 233,
    "728": 145,
    "729": 169,
    "730": 9,
    "731": 1,
    "732": 247,
    "733": 169,
    "734": 233,
    "735": 145,
    "736": 169,
    "737": 9,
    "738": 13,
    "739": 180,
    "740": 6,
    "741": 1,
    "742": 2,
    "743": 215,
    "744": 146,
    "745": 183,
    "746": 208,
    "747": 10,
    "748": 186,
    "749": 5,
    "750": 3,
    "751": 1,
    "752": 176,
    "753": 247,
    "754": 241,
    "755": 159,
    "756": 9,
    "757": 129,
    "758": 248,
    "759": 241,
    "760": 159,
    "761": 9,
    "762": 136,
    "763": 176,
    "764": 247,
    "765": 241,
    "766": 159,
    "767": 9,
    "768": 128,
    "769": 169,
    "770": 233,
    "771": 145,
    "772": 169,
    "773": 9,
    "774": 1,
    "775": 178,
    "776": 5,
    "777": 3,
    "778": 1,
    "779": 176,
    "780": 247,
    "781": 241,
    "782": 159,
    "783": 9,
    "784": 129,
    "785": 248,
    "786": 241,
    "787": 159,
    "788": 9,
    "789": 136,
    "790": 176,
    "791": 247,
    "792": 241,
    "793": 159,
    "794": 9,
    "795": 128,
    "796": 169,
    "797": 233,
    "798": 145,
    "799": 169,
    "800": 9,
    "801": 1,
    "802": 140,
    "803": 5,
    "804": 2,
    "805": 1,
    "806": 176,
    "807": 247,
    "808": 241,
    "809": 159,
    "810": 9,
    "811": 129,
    "812": 248,
    "813": 241,
    "814": 159,
    "815": 9,
    "816": 136,
    "817": 176,
    "818": 247,
    "819": 241,
    "820": 159,
    "821": 9,
    "822": 230,
    "823": 4,
    "824": 1,
    "825": 2,
    "826": 196,
    "827": 204,
    "828": 214,
    "829": 140,
    "830": 5,
    "831": 221,
    "832": 4,
    "833": 1,
    "834": 2,
    "835": 153,
    "836": 156,
    "837": 249,
    "838": 163,
    "839": 9,
    "840": 212,
    "841": 4,
    "842": 1,
    "843": 2,
    "844": 255,
    "845": 128,
    "846": 221,
    "847": 206,
    "848": 10,
    "849": 203,
    "850": 4,
    "851": 1,
    "852": 2,
    "853": 233,
    "854": 232,
    "855": 175,
    "856": 138,
    "857": 4,
    "858": 194,
    "859": 4,
    "860": 1,
    "861": 2,
    "862": 221,
    "863": 191,
    "864": 251,
    "865": 211,
    "866": 8,
    "867": 185,
    "868": 4,
    "869": 1,
    "870": 2,
    "871": 223,
    "872": 204,
    "873": 205,
    "874": 137,
    "875": 11,
    "876": 176,
    "877": 4,
    "878": 1,
    "879": 2,
    "880": 147,
    "881": 178,
    "882": 232,
    "883": 208,
    "884": 5,
    "885": 167,
    "886": 4,
    "887": 1,
    "888": 2,
    "889": 228,
    "890": 161,
    "891": 231,
    "892": 155,
    "893": 13,
    "894": 158,
    "895": 4,
    "896": 1,
    "897": 2,
    "898": 252,
    "899": 136,
    "900": 161,
    "901": 138,
    "902": 5,
    "903": 149,
    "904": 4,
    "905": 1,
    "906": 2,
    "907": 226,
    "908": 245,
    "909": 236,
    "910": 135,
    "911": 2,
    "912": 138,
    "913": 4,
    "914": 1,
    "915": 2,
    "916": 181,
    "917": 193,
    "918": 229,
    "919": 73,
    "920": 222,
    "921": 1,
    "922": 1,
    "923": 1,
    "924": 240,
    "925": 165,
    "926": 255,
    "927": 235,
    "928": 12,
    "929": 3,
    "930": 40,
    "931": 1,
    "932": 5,
    "933": 110,
    "934": 111,
    "935": 228,
    "936": 1,
    "937": 101,
    "938": 243,
    "939": 2,
    "940": 15,
    "941": 178,
    "942": 3,
    "943": 95,
    "944": 177,
    "945": 4,
    "946": 55,
    "947": 182,
    "948": 5,
    "949": 54,
    "950": 178,
    "951": 6,
    "952": 52,
    "953": 181,
    "954": 7,
    "955": 51,
    "956": 185,
    "957": 8,
    "958": 50,
    "959": 178,
    "960": 9,
    "961": 49,
    "962": 181,
    "963": 10,
    "964": 1,
    "965": 246,
    "966": 10,
    "967": 4,
    "968": 130,
    "969": 11,
    "970": 105,
    "971": 228,
    "972": 12,
    "973": 119,
    "974": 143,
    "975": 14,
    "976": 50,
    "977": 223,
    "978": 14,
    "979": 49,
    "980": 183,
    "981": 15,
    "982": 54,
    "983": 182,
    "984": 16,
    "985": 50,
    "986": 180,
    "987": 17,
    "988": 53,
    "989": 179,
    "990": 18,
    "991": 57,
    "992": 178,
    "993": 19,
    "994": 50,
    "995": 177,
    "996": 20,
    "997": 53,
    "998": 132,
    "999": 21,
    "1000": 116,
    "1001": 249,
    "1002": 21,
    "1003": 112,
    "1004": 229,
    "1005": 23,
    "1006": 119,
    "1007": 139,
    "1008": 25,
    "1009": 68,
    "1010": 229,
    "1011": 25,
    "1012": 102,
    "1013": 245,
    "1014": 26,
    "1015": 108,
    "1016": 244,
    "1017": 27,
    "1018": 45,
    "1019": 206,
    "1020": 28,
    "1021": 111,
    "1022": 228,
    "1023": 29,
    "1024": 101,
    "1025": 132,
    "1026": 31,
    "1027": 100,
    "1028": 225,
    "1029": 32,
    "1030": 116,
    "1031": 225,
    "1032": 33,
    "1033": 118,
    "1034": 130,
    "1035": 35,
    "1036": 9,
    "1037": 244,
    "1038": 35,
    "1039": 97,
    "1040": 226,
    "1041": 36,
    "1042": 108,
    "1043": 229,
    "1044": 37,
    "1045": 78,
    "1046": 225,
    "1047": 38,
    "1048": 109,
    "1049": 2,
    "1050": 42,
    "1051": 1,
    "1052": 5,
    "1053": 110,
    "1054": 111,
    "1055": 228,
    "1056": 1,
    "1057": 101,
    "1058": 243,
    "1059": 2,
    "1060": 15,
    "1061": 177,
    "1062": 3,
    "1063": 102,
    "1064": 177,
    "1065": 4,
    "1066": 55,
    "1067": 182,
    "1068": 5,
    "1069": 54,
    "1070": 178,
    "1071": 6,
    "1072": 52,
    "1073": 181,
    "1074": 7,
    "1075": 55,
    "1076": 176,
    "1077": 8,
    "1078": 62,
    "1079": 177,
    "1080": 9,
    "1081": 60,
    "1082": 177,
    "1083": 10,
    "1084": 2,
    "1085": 181,
    "1086": 10,
    "1087": 1,
    "1088": 183,
    "1089": 10,
    "1090": 2,
    "1091": 246,
    "1092": 10,
    "1093": 4,
    "1094": 130,
    "1095": 11,
    "1096": 105,
    "1097": 228,
    "1098": 12,
    "1099": 119,
    "1100": 143,
    "1101": 14,
    "1102": 56,
    "1103": 223,
    "1104": 14,
    "1105": 49,
    "1106": 183,
    "1107": 15,
    "1108": 54,
    "1109": 182,
    "1110": 16,
    "1111": 50,
    "1112": 179,
    "1113": 17,
    "1114": 54,
    "1115": 179,
    "1116": 18,
    "1117": 58,
    "1118": 177,
    "1119": 19,
    "1120": 56,
    "1121": 177,
    "1122": 20,
    "1123": 61,
    "1124": 132,
    "1125": 21,
    "1126": 116,
    "1127": 249,
    "1128": 21,
    "1129": 112,
    "1130": 229,
    "1131": 23,
    "1132": 119,
    "1133": 139,
    "1134": 25,
    "1135": 68,
    "1136": 229,
    "1137": 25,
    "1138": 102,
    "1139": 245,
    "1140": 26,
    "1141": 108,
    "1142": 244,
    "1143": 27,
    "1144": 45,
    "1145": 206,
    "1146": 28,
    "1147": 111,
    "1148": 228,
    "1149": 29,
    "1150": 101,
    "1151": 132,
    "1152": 31,
    "1153": 100,
    "1154": 225,
    "1155": 32,
    "1156": 116,
    "1157": 225,
    "1158": 33,
    "1159": 118,
    "1160": 130,
    "1161": 35,
    "1162": 9,
    "1163": 244,
    "1164": 35,
    "1165": 97,
    "1166": 226,
    "1167": 36,
    "1168": 108,
    "1169": 229,
    "1170": 37,
    "1171": 78,
    "1172": 225,
    "1173": 38,
    "1174": 109
  }

  const loadDigram = async () => {
    try {
      // 1. Fetch Metadata (JSON)
      const meta = await fetchDiagramMetadata(roomId);
      console.log("Metadata loaded:", meta);

      updateSchemaName(meta.diagramName);
      setIsReadOnly(meta.role === "READER");

      // 2. Fetch Snapshot (Binary)
      const snapshotBuffer = await fetchDiagramSnapshot(roomId);
      console.log(new Uint8Array(snapshotBuffer))
      console.log(`Snapshot loaded: ${snapshotBuffer.byteLength} bytes`);

      // Pass the binary buffer directly to loadCompositeYjsData
      // (Note: need to update loadCompositeYjsData to handle ArrayBuffer)
      // loadCompositeYjsData(snapshotBuffer);
      // loadCompositeYjsData(new Uint8Array(snapshotBuffer));
      loadCompositeYjsData(new Uint8Array(x));

    } catch (err) {
      showError(err.message);
    }
  };

  useEffect(() => {
    loadDigram();
    // if (ydoc && roomId) {
    // }
  }, []);
  // }, [ydoc, roomId]);

  useEffect(() => {
    const handleKeyDown = (e) => {
      // Check for Ctrl (Windows) or Meta (Mac)
      if (e.ctrlKey || e.metaKey) {
        if (e.key === "z") {
          e.preventDefault();
          if (e.shiftKey) {
            redo(); // Ctrl + Shift + Z
          } else {
            undo(); // Ctrl + Z
          }
        } else if (e.key === "y") {
          e.preventDefault();
          redo(); // Ctrl + Y
        }
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [undo, redo]);

  const takeSnapshot = async () => {
    const viewport = document.querySelector(".react-flow__viewport");

    await reactFlowInstance.fitView({ padding: 50 });

    if (!viewport) return;

    try {
      const thumbnail = await toPng(viewport, {
        backgroundColor: "#ffffff",
        quality: 1,
      });

      return thumbnail;
    } catch (err) {
      console.log("Error exporting:", err);
    }
  };

  const onConnect = useCallback(
    (params) => {
      if (!params || !params.source || !params.target) return;

      applyRelationLogic(
        params.source,
        params.target,
        selectedRelationType,
        nodes,
        { updateNodeData, addNodeYjs, addEdgeYjs }
      );

      const typeKey =
        {
          "1:1": "oneToOne",
          "1:N": "oneToMany",
          "N:1": "manyToOne",
          "M:N": "manyToMany",
        }[selectedRelationType] || "oneToMany";

      const newEdge = {
        source: params.source,
        target: params.target,
        id: `e_${params.source}_${params.target}_${Date.now()}`,
        type: typeKey,
        data: { type: selectedRelationType },
      };
      addEdgeYjs(newEdge);
    },
    [selectedRelationType, addEdgeYjs, nodes, updateNodeData, addNodeYjs]
  );

  const addNode = () => {
    const id = `${nodes.length + 1}_${Date.now()}`;
    const newNode = {
      id,
      type: "Defult-Node", // Make sure this matches your nodeTypes key
      data: {
        tableName: `Entity_${nodes.length + 1}`,
        columns: [
          {
            id: `attr1_${id}`,
            name: "id",
            dataType: "INT",
            constraints: { PRIMARY_KEY: true },
          },
        ],
      },
      position: { x: Math.random() * 400, y: Math.random() * 400 },
    };
    addNodeYjs(newNode);
  };

  const onSaveDiagram = async () => {
    if (!roomId) {
      showError("Diagram ID is missing. Cannot save.");
      return;
    }

    setIsSaving(true);

    const thumbnailPNG = await takeSnapshot();

    const thumbnailURL = await uploadToCloudinary(thumbnailPNG, roomId);

    const binaryState = Y.encodeStateAsUpdate(ydoc);
    const base64State = uint8ArrayToBase64(binaryState);

    const payload = {
      diagramName: schemaName,
      state: base64State,
      picture: thumbnailURL,
    };

    try {
      await updateDiagram(roomId, payload);
      await takeSnapshot();
      showSuccess("Diagram saved successfully!");
    } catch (err) {
      showError(err.message);
    } finally {
      setIsSaving(false);
    }
  };

  const onGenerateSQL = async () => {
    const validation = validateSchema(nodes);

    if (!validation.isValid) {
      showError(`Validation Failed:\n- ${validation.errors.join("\n- ")}`);
      return;
    }

    const finalJson = convertToJSON(schemaName, nodes);

    try {
      const data = await generateSQLFromBackend(finalJson);
      setGeneratedSql(data);
      setIsSqlPanelOpen(true);
    } catch (err) {
      showError(err.message);
      setIsSqlPanelOpen(false);
    }
  };

  return (
    <div className="drawing-container" onMouseMove={onMouseMove}>
      {isSqlPanelOpen && (
        <CodeEditor
          initialCode={generatedSql}
          onClose={() => setIsSqlPanelOpen(false)}
        />
      )}

      <input
        className="schema-name"
        placeholder="Database Name"
        value={schemaName}
        onChange={(e) => updateSchemaName(e.target.value)}
        disabled={isReadOnly}
      />
      <div className="active-users">
        <ActiveUsers />
      </div>
      <div >
        <button className="save-btn" onClick={onSaveDiagram}>Save</button>
      </div>

      <div className="drawing-canva">
        <ReactFlow
          onInit={(instance) => setReactFlowInstance(instance)}
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          nodeTypes={nodeTypes}
          edgeTypes={edgeTypes}
          fitView
          connectionMode="loose"
          nodesDraggable={!isReadOnly}
          nodesConnectable={!isReadOnly}
          elementsSelectable={!isReadOnly}
          zoomOnDoubleClick={!isReadOnly}
          defaultEdgeOptions={{
            style: { strokeWidth: 2, stroke: "#94a3b8" },
          }}
          //new props
          proOptions={{ hideAttribution: true }}
          nodeDragThreshold={2}
          onlyRenderVisibleElements={true}
        >
          <MiniMap
            style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
            nodeColor="#cbd5e1"
            maskColor="rgba(241, 245, 249, 0.6)"
          />
          {!isReadOnly && (
            <Controls
              style={{
                borderRadius: 8,
                overflow: "hidden",
                border: "none",
                boxShadow: "0 4px 6px -1px rgba(0,0,0,0.1)",
              }}
            />
          )}
          <Background color="#5f5f5fff" gap={20} size={1} variant="dots" />
          <CursorLayer>
            {cursors.map((cursor) => (
              <Cursor
                key={cursor.id}
                x={cursor.x}
                y={cursor.y}
                color={cursor.color}
                name={cursor.name}
              />
            ))}
          </CursorLayer>
        </ReactFlow>
      </div>

      {!isSqlPanelOpen && !isReadOnly && (
        <div className="toolbar-container">
          <Toolbar
            addNode={addNode}
            selectedRelationType={selectedRelationType}
            setSelectedRelationType={setSelectedRelationType}
            undo={undo}
            redo={redo}
          />


        </div>

      )}
      <button className="generate" onClick={onGenerateSQL}>
        Generate SQL
      </button>
    </div>
  );
};

export default function Schema() {
  const { roomId } = useParams();
  return (
    <ReactFlowProvider>
      <CollaborationProvider roomId={roomId}>
        <SchemaContent />
      </CollaborationProvider>
    </ReactFlowProvider>
  );
}

const CursorLayer = ({ children }) => {
  const { x, y, zoom } = useViewport();

  return (
    <div
      style={{
        position: "absolute",
        top: 0,
        left: 0,
        pointerEvents: "none",
        zIndex: 1000,
        transform: `translate(${x}px, ${y}px) scale(${zoom})`,
        transformOrigin: "0 0",
        width: "100%",
        height: "100%",
      }}
    >
      {children}
    </div>
  );
};
