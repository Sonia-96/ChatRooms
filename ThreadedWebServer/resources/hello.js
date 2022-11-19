// elements
let userTA = document.getElementById("userNameTA");
let roomTA = document.getElementById("roomNameTA");
let userColumn = document.getElementById("userColumn");
let messageColumn = document.getElementById("messageColumn");
let message = document.getElementById("messageTA");
let roomMenu = document.getElementById("roomMenu");

// variables
let username = null;
let rooms = new Map(); // hold information of each room, each room has userList, messageList, set of usernames
let currentRoom = null;

userTA.addEventListener("keypress", enterRoomCB);
roomTA.addEventListener("keypress", enterRoomCB);
message.addEventListener("keypress", sendMessageCB);

roomMenu.onchange = function changeRoom() {
    let value = roomMenu.value;
    console.log("You choose " + value);
    let room = rooms.get(value);
    let messageList = document.getElementById("messageList");
    let userList = document.getElementById("userList");
    userColumn.removeChild(userList);
    messageColumn.removeChild(messageList);
    userColumn.appendChild(room.userList);
    messageColumn.appendChild(room.messageList);
    currentRoom = room;
}

// when closing the window, send leave message to the server
window.addEventListener("beforeunload", () => {
    let message = {"type": "leave", "user": username, "room": currentRoom.name, "time": getTimestamp()};
    ws.send(JSON.stringify(message));
})

function enterRoomCB(event) {
    if (event.key === "Enter") {
        event.preventDefault();
        let roomName = roomTA.value;
        // check if the roomname is valid
        for (let c of roomName) {
            if (c < 'a' || c > 'z') {
                alert("The room name should be lowercase and no space!");
                roomTA.value = "";
                roomTA.select();
                event.preventDefault();
                return;
            }
        }
        if (rooms.has(roomName) && rooms.get(roomName).users.has(username)) {
            alert("You are already in the room!");
            return;
        }
        // check if the username is valid
        username = userTA.value;
        // TODO set username as unchangeable
        if (username === "") {
            alert("The username can't be empty!");
            username.value = "<Enter a Username>";
            userTA.select();
            event.preventDefault();
            return;
        }
        // set username read only after entering the room
        userTA.readOnly = true;
        roomMenu.value = roomName;
        if (wsOpen) {
            let message = {"type": "join", "user": username, "room": roomName, "time": getTimestamp()};
            ws.send(JSON.stringify(message));
        }
    }
}

function sendMessageCB(event) {
    if (event.key === "Enter") {
        if (currentRoom == null) {
            alert("You should first enter a room!");
            return;
        }
        if (!currentRoom.users.has(username)) {
            alert("You should first enter the room!");
            return;
        }
        let messageContent = message.value;
        if (wsOpen) {
            let message = {"type": "message", "user": username, "room": currentRoom.name, "message": messageContent, "time": getTimestamp()};
            ws.send(JSON.stringify(message));
            this.value = "";
            this.select();
            event.preventDefault();
        }
    }
}

function addUser(room, userName) {
    if (!room.users.has(userName)) {
        let li = document.createElement('li');
        li.textContent = userName;
        li.setAttribute("id", userName);
        room.userList.appendChild(li);
        room.users.add(userName);
    }
}

function addMessage(room, message) {
    let p = document.createElement('p');
    p.innerHTML = message;
    room.messageList.appendChild(p);
    p.scrollIntoView(false);
}

function handleMessageCB(event) {
    let msgObj = JSON.parse(event.data);
    console.log(msgObj);
    let type = msgObj.type, userName = msgObj.user, roomName = msgObj.room, time = msgObj.time;
    if (type === "roomInfo") {
        console.log("This is room info");
        for (let room in msgObj) {
            addRoomForRoomsInfo(room, msgObj[room]);
        }
        currentRoom = rooms.get(roomTA.value);
        userColumn.appendChild(currentRoom.userList);
        messageColumn.appendChild(currentRoom.messageList);
    } else if (type === "join") {
        console.log("This is join " + roomName);
        if (rooms.has(roomName)) {
            let room = rooms.get(roomName);
            addUser(room, userName);
            addMessage(room, `<b>[${time}] ${userName} joins the ${roomName}</b>.`)
        } else {
            addRoomForJoinMessage(msgObj);
        }
    } else if (type === "message") {
        console.log("This is message");
        let room = rooms.get(roomName);
        addMessage(room, `[${time}] ${userName}: ${msgObj.message}`)
    } else if (type === "leave") {
        let room = rooms.get(roomName);
        addMessage(room, `<b>[${time}] ${userName} leaves the ${roomName}</b>.`)
        let li = room.userList.querySelector("#" + userName);
        li.remove();
    }
}

let wsOpen = false;
function handleConnectCB(event) {
    wsOpen = true;
}

function handleErrorCB(event) {
    console.log("Web Socket Connection Error!");
}

function handleCloseCB() {
    wsOpen = false;
}

// Should I create ws after the user enter the room?
let ws = new WebSocket("ws://localhost:8080");
ws.onopen = handleConnectCB;
ws.onerror = handleErrorCB;
ws.onclose = handleCloseCB;
ws.onmessage = handleMessageCB;

let getTimestamp = function() {
    const today = new Date();
    const date = today.getFullYear() + '-' + today.getMonth() + '-' + today.getDate();
    const time = today.getHours() + ':' + today.getMinutes() + ':' + today.getSeconds();
    return date + ' ' + time;
}

function addRoomForJoinMessage(joinJson) {
    let userName = joinJson.user, roomName = joinJson.room, time = joinJson.time;
    let room = createNewRoom(roomName);
    addUser(room, userName);
    addMessage(room, `<b>[${time}] ${userName} joins ${roomName}</b>`);
    addOptionToDropdownMenu(roomName);
}

let addRoomForRoomsInfo = function (roomname, roomJson) {
    if (roomname === "type") return; // TODO 修改json格式：{rooms:[room1, room2, ...], type}
    console.log("add a room: " + roomname);
    let room = createNewRoom(roomname);
    for (let i = 0; i < roomJson.clients.length; i++) {
        let clientObj = roomJson.clients[i];
        addUser(room, clientObj);
    }
    for (let i = 0; i < roomJson.messages.length; i++) {
        let msgObj = roomJson.messages[i];
        let type = msgObj.type, user = msgObj.user, roomName = msgObj.room, time = msgObj.time, msg = msgObj.message;
        if (type === "join") {
            addMessage(room, `<b>[${time}] ${user} joins ${roomName}</b>`);
        } else if (type === "message") {
            addMessage(room, `[${time}] ${user}: ${msg}`);
        } else if (type === "leave") {
            addMessage(room, `<b>[${time}] ${user} leaves ${roomName}</b>`);
        }
    }
    addOptionToDropdownMenu(roomname);
}

function createNewRoom(roomname) {
    let room = {"name": roomname};
    rooms.set(roomname, room);
    room.userList = document.createElement("ul");
    room.userList.setAttribute("id", "userList")
    room.messageList = document.createElement("div");
    room.messageList.setAttribute("id", "messageList");
    room.users = new Set();
    return room;
}

function addOptionToDropdownMenu(roomname) {
    let option = document.createElement("option");
    option.value = roomname;
    option.textContent = roomname;
    roomMenu.appendChild(option);
}