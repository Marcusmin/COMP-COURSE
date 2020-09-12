// importing named exports we use brackets
import { createPostTile, uploadImage, uploadedModal, getUserInfoById } from './helpers.js';

// when importing 'default' exports, use below syntax
// import API from './api.js';

// const api  = new API();

// // we can use this single api request multiple times
// const feed = api.getFeed();

// feed
// .then(posts => {
//     posts.reduce((parent, post) => {

//         parent.appendChild(createPostTile(post));
        
//         return parent;

//     }, document.getElementById('large-feed'))
// });

// Potential example to upload an image
// const input = document.querySelector('input[type="file"]');

// input.addEventListener('change', uploadImage);

//implement a login function
var login = document.getElementById('loginButton')
var registration = document.getElementById('registrationButton')
var updateProflieButton = document.getElementById('updateProfile')
updateProflieButton.addEventListener('click', updateProfileChange)
var updateSubmitButton = document.getElementById('profileUpdateSubmit')
updateSubmitButton.addEventListener('click', submitProfileChange)
var followButton = document.getElementById("followSubmit")
followButton.addEventListener('submit', (event)=>{
    event.preventDefault()
})
followButton.addEventListener('click', ()=>{
    let username = document.getElementById('followUsername').value
    followAUserByUsername(username)
})
import {createElement} from './helpers.js'

function createUserInfoButton(){
    let logout = createElement('button', 'logout', {class: "dropdown-item", id: "logout"})
    logout.addEventListener("click", hasLogout)
    let userInfoDive = createElement('div', null,{class:"dropdown"})
    // let userInfoIcon = createElement('i', "account_box", {class:"material-icons"})
    userInfoDive.appendChild
    let userInfoButton = createElement('button', "account", {
        class:"btn btn-secondary dropdown-toggle",
        type:"button", 
        id:"dropdownMenuButton",
        "data-toggle":"dropdown",
        "aria-haspopup":"true",
        "aria-expanded":"false"})
    userInfoDive.appendChild(userInfoButton)
    let userInfoDiveDropDownMenu = createElement('div', null, {class:"dropdown-menu", "aria-labelledby":"dropdownMenuButton"})
    // userInfoButton.appendChild(userInfoIcon)
    userInfoButton.appendChild(userInfoDiveDropDownMenu)
    let profileButton = createElement('button', 'profile', {
        class: "dropdown-item", 
        id: "profileButton",
        "data-toggle":"modal",
        "data-target":"#profile"
    })
    profileButton.addEventListener('click', getCurUserProfile)
    userInfoDiveDropDownMenu.appendChild(profileButton)
    userInfoDiveDropDownMenu.appendChild(logout)
    userInfoDive.appendChild(userInfoDiveDropDownMenu)
    return userInfoDive
}
function getCurUserProfile(){
    let getUserInfoURL = new URL("http://127.0.0.1:5000/user/")
    console.log(`requiring current's message`)
    let token = sessionStorage.getItem("token")
    fetch(getUserInfoURL,{
        method:"GET",
        headers:{
            Authorization: `Token ${token}`,
        },
    })
    .then((res)=>{
        return res.json()})
    .then((res)=>{
        console.log(res)
        showProfle(res)
    })
    .catch((e)=>{
        console.log(e)
    })
}
function showProfle(json){
    let userName = json.name
    let userEmail = json.email
    let howManyPost = json.posts.length
    let nbOfFollower = json.followed_num
    let nbOfFollowing = json.following.length
    let userPassword = sessionStorage.getItem('currentUserPassword')
    let item = [userName, userEmail, userPassword, howManyPost, nbOfFollower, nbOfFollowing]
    let profileList = document.getElementById("profile").getElementsByTagName('input')
    for (const i in item){
        profileList[i].value = `${item[i]}`
        // profileList[i].appendChild(document.createTextNode(`${item[i]}`))
    }
    // let closeButton = document.getElementById("profileClose")
    // closeButton.addEventListener('click', ()=>{
    //     for (const i in item){
    //         console.log(profileList[i].lastChild)
    //         profileList[i].removeChild(profileList[i].lastChild)
    //     }
    // })
}
var userInfoDive = createUserInfoButton()
var uploadButton = createElement('button', "upload", {
                                            class: "btn btn-light",
                                            id: "upload",
                                            "data-toggle": "modal",
                                            "data-target": "#exampleModal"
                                        })
//if press upload, add img in modal
uploadButton.addEventListener('click', uploadedModal)
controlLogin()
controlRegistration()
function controlLogin(){
    let submitButton = document.getElementById('loginSubmit')
    submitButton.addEventListener('click', checkIsValidLogin)
}

function controlRegistration(){
    let submitButton = document.getElementById("registrationSubmit")
    submitButton.addEventListener('click', ()=>{
        let registrationForm = document.getElementById("registrationForm")
        registrationForm.addEventListener("submit", (event)=>{
            event.preventDefault()
        })
        let signupURL = "http://127.0.0.1:5000/auth/signup"
        let registrationFormData = new FormData(registrationForm)
        let data = {}
        registrationFormData.forEach((value, key)=>{
            data[key] = value
        })
        let dataJson = JSON.stringify(data)
        fetch(signupURL, {
            method: "POST",
            body: dataJson,
            headers:{
                'Content-Type': 'application/json'
            }
        })
        .then((res)=>res.status)
        .then((status)=>{
            if(status == 400){
                alert("Malformed Request")
            }else if(status == 200){
                alert("Registration success")
                $('#registration').modal('hide')
                cleanupForm(registrationForm)
            }else if(status == 409){
                alert("Username Taken")
            }
        })
        .catch((e)=>{console.log(e)})
    })
        // })
    // }
}
function cleanupForm(form){
    for (const i of form.getElementsByTagName('input')){
        i.value = null
    }
}

function checkIsValidLogin(){
    let loginForm = document.getElementById("loginForm")
    //console.log(loginForm)
    loginForm.addEventListener("submit", (event)=>{
        event.preventDefault()
    })
    let authURL = "http://127.0.0.1:5000/auth/login"
    let loginFormData = new FormData(loginForm)
    let data = {}
    loginFormData.forEach((value, key)=>{
        data[key] = value
    })
    let dataJson = JSON.stringify(data)
    fetch(authURL, {
        method: "POST",
        body: dataJson,
        headers:{
            'Content-Type': 'application/json'
        }
    })
    .then((res)=>{
        let status = res.status
        let token = res.json()  //remind this is a promise
        return {
            "status": status,
            "token": token
        }
    })
    .then((res)=>{
        if(res.status == 403){
            alert("invalid user message")
        }else if(res.status == 200){
            alert("Hello")
            res.token
            .then((res)=>{
                // if user inforamtion is valid, store token
                sessionStorage.setItem("token", res.token)
                sessionStorage.setItem("currentUserPassword", data["password"])
                sessionStorage.setItem("currentUserName", data["username"])
                requirePost(1)
                $('#login').modal('hide')
                cleanupForm(loginForm)
            })
            //do something when user login
            hasLogin()
            console.log("Has login")
        }else if(res.status == 400){
            alert("Missing username/password")
        }
    })
    .catch((e)=>{console.log(e)})
}

function requirePost(pageNumber){
    let token = sessionStorage.getItem("token")
    //console.log(token)
    let postURL = new URL("http://127.0.0.1:5000/user/feed")
    //each page has 5 posts
    postURL.searchParams.append("n", '5')
    postURL.searchParams.append('p', ((pageNumber - 1) * 5).toString())
    let requireInit = {
        method: "GET",
        headers: {
            'Content-Type': 'application/json',
            "Authorization": `Token ${token}`
        }
    }
    fetch(postURL, requireInit)
    .then((res)=>{
        let status = res.status
        let posts = res.json()  //remind this is a promise
        return {
            "status": status,
            "posts": posts
        }
    })
    .then((res)=>{
        if(res.status == 403){
            console.log("Invalid Auth Token")
        }else if(res.status == 200){
            res.posts
            .then((res) =>{
                let posts = res.posts
                console.log(`${posts} is ${pageNumber}`)
                posts.reduce((parent, post) => {
                    parent.appendChild(createPostTile(post));    
                    return parent;
                    }, document.getElementById('large-feed'))
                sessionStorage.setItem("currentPage", `${pageNumber}`)
                sessionStorage.setItem("postsLength", `${res.posts.length}`)
                return posts.length
            })
        }
    })
}
//after log in, show the user logout button
function hasLogin(){
    if(login.parentNode){
        login.parentNode.removeChild(login)
    }
    // let registration = document.getElementById('registrationButton')
    if(registration.parentNode){
        registration.parentNode.removeChild(registration)
    }
    let navList = document.getElementsByClassName('nav-item')
    navList[0].appendChild(uploadButton)
    navList[1].appendChild(userInfoDive)
    //enable page navigation
    let pageNav = document.getElementById("Pagination")
    // pageNav.addEventListener("click", enableNavigation(pageNav))
    let pageList = pageNav.getElementsByTagName('li')
    for (const i in pageList) {
        if (i >= 1 && i <= 3){
            pageList[i].addEventListener("click", goToPaticularPage(i))
        }else{
            if (i == 0)
            pageList[i].addEventListener("click", backToPrevious)
            if (i == 4)
            pageList[i].addEventListener('click', goToNextPage)
        }
    }
}
//when user press log out, clear the page and clean the storage
function hasLogout(){
    let li = userInfoDive.parentNode
    li.removeChild(userInfoDive)
    li.appendChild(registration)
    li = uploadButton.parentNode
    li.removeChild(uploadButton)
    li.appendChild(login)
    let content = document.getElementById("large-feed")
    while(content.firstChild){
        console.log(content.firstChild)
        content.removeChild(content.firstChild)
    }
    sessionStorage.clear()
}
function goToPaticularPage(i){
    //clean the current feed and require new post
    function __goToPaticularPage(){
        //clear
        cleanCurFeed()
        console.log(`Going to page${i}`)
        requirePost(i)
    }
    return __goToPaticularPage
}

function backToPrevious(){
    let currentPage = parseInt(sessionStorage.getItem("currentPage"))
    if (currentPage == 1){
        //
    }else if(currentPage > 1){
        cleanCurFeed()
        requirePost(currentPage - 1)
    }
}
function goToNextPage(){
    let currentPage = parseInt(sessionStorage.getItem("currentPage"))
    let postsLegnth = parseInt(sessionStorage.getItem('postsLength'))
    cleanCurFeed()
    if(postsLegnth > 0){
        requirePost(currentPage + 1)
    }
}
function cleanCurFeed(){
    let feed = document.getElementById('large-feed')
    while (feed.firstChild){
        console.log(`remove ${feed.firstChild}`)
        feed.removeChild(feed.firstChild)
    }
}

function updateProfileChange(){
    let changeableField = document.getElementsByClassName('changeable')
    console.log("require change profile message")
    for (const b of changeableField){
        console.log(b)
        b.disabled = false
    }
}
function submitProfileChange(){
    //communicate with backend
    let changedData = {}
    let changeableField = document.getElementsByClassName('changeable')
    let putUserURL = new URL("http://127.0.0.1:5000/user/")
    let token = sessionStorage.getItem('token')
    for (const b of changeableField){
        changedData[`${b.name}`] = b.value
    }
    console.log(changedData)
    console.log(token)
    if (changedData["password"].length < 1){
        alert("Password length error")
        return
    }
    fetch(putUserURL, {
        method: "PUT",
        headers: {
            "Authorization": `Token ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(changedData)
    })
    .then((res)=>{
        if(res.status == 200){
            alert("change success")
            for (const b of changeableField){
                //console.log(b)
                //disable changeable field
                b.disabled = true
            }
        }else if(res.status == 400){
            alert("Malformed user object")
        }else if(res.status == 403){
            alert("Invalid Authorization Token")
        }
    })
}
function followAUserByUsername(username){
    let token = sessionStorage.getItem('token')
    let followURL = new URL("http://127.0.0.1:5000/user/follow")
    followURL.searchParams.append('username', username)
    fetch(followURL, {
        method: "PUT",
        headers:{
            "Authorization": `Token ${token}`,
            'Content-Type': 'application/json'
        }
    })
    .then((res)=>{
        if(res.status == 200){
            alert("Follow Success")
        }else{
            alert("Follow Fail")
        }
    })
}
