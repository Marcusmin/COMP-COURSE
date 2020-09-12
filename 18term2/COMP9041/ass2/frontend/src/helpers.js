
/* returns an empty array of size max */
export const range = (max) => Array(max).fill(null);

/* returns a randomInteger */
export const randomInteger = (max = 1) => Math.floor(Math.random()*max);

/* returns a randomHexString */
const randomHex = () => randomInteger(256).toString(16);

/* returns a randomColor */
export const randomColor = () => '#'+range(3).map(randomHex).join('');

/**
 * You don't have to use this but it may or may not simplify element creation
 * 
 * @param {string}  tag     The HTML element desired
 * @param {any}     data    Any textContent, data associated with the element
 * @param {object}  options Any further HTML attributes specified
 */
export function createElement(tag, data, options = {}) {
    const el = document.createElement(tag);
    el.innerHTML = data;
   
    // Sets the attributes in the options object to the element
    return Object.entries(options).reduce(
        (element, [field, value]) => {
            element.setAttribute(field, value);
            return element;
        }, el);
}

/**
 * Given a post, return a tile with the relevant data
 * @param   {object}        post 
 * @returns {HTMLElement}
 */
export function createPostTile(post) {
    const section = createElement('section', null, { class: 'post'});
    let postTitle = createElement('h2', post.meta.author, { class: 'post-title' })
    postTitle.addEventListener('click',function(){
        $('#userPageModal').modal('show')
    });
    postTitle.addEventListener('click', showUserPage(post.meta.author))
    section.appendChild(postTitle)
    section.appendChild(createElement('img', null, {
        src: 'data:image/jpg;base64,' + post.src,
        alt: post.meta.description_text, 
        class: 'post-image rounded-top' ,
    }));
    let dayOfPublished = new Date(parseFloat(post.meta.published * 1000))
    let time = `Published at ${dayOfPublished.getFullYear()}.${dayOfPublished.getMonth() + 1}.${dayOfPublished.getDate()} ${dayOfPublished.getHours()}:${dayOfPublished.getMinutes()}`
    //text container contain the discription of the picture
    let textContainer = createElement("span", null, {class:"text-container"})
    //icon container contains the icons(likes, comment)
    let iconContainer = createElement('span', null, {class: "icon-container"})
    //textContainer.appendChild(createElement("h5", post.meta.likes.length+" likes", { class: 'post-likes' }))
    //textContainer.appendChild(createElement("h5", "comments: "+post.comments.length, { class: 'post-comments' }))
    textContainer.appendChild(createElement("p", post.meta.author+': '+post.meta.description_text, { class: 'post-discription font-italic' }))
    let likeIcon = createElement('i', "favorite_border", {class: "material-icons"})
    let whoCommentButton = createElement("button",post.comments.length+" comments",{
        type: "button",
        class: "btn btn-light who-comments",
        "data-toggle": "modal",
        "data-target": "#exampleModal",
        "post-id": post.id
    })
    //iconContainer.appendChild(commentsIcon)
    let whoLikesButton = createElement("button",post.meta.likes.length+" likes",{
        type: "button",
        class: "btn btn-light who-likes-button",
        "data-toggle": "modal",
        "data-target": "#exampleModal",
        "post-id": post.id
    })
    likeIcon.addEventListener("click", userLikeThisPost(post.id))
    //let commentsIcon = createElement('i', "comment", {class: "material-icons"})
    iconContainer.appendChild(likeIcon)
    iconContainer.appendChild(whoLikesButton)
    textContainer.appendChild(whoCommentButton)
    textContainer.appendChild(createCommentInputGroup(post.id))
    section.appendChild(createElement("small",time, { class: 'post-time' }))
    section.appendChild(iconContainer)
    //section.appendChild(likeButtonContainer)
    section.appendChild(textContainer)
    whoLikesButton.addEventListener("click", showWhoLikes(post.meta.likes))
    whoCommentButton.addEventListener("click", showWhoComment(post.comments, post.id, whoCommentButton))
    return section;
}
function showWhoLikes(list){
    function __showWoLikes(){
        let whoLikesModalBody = document.getElementsByClassName("modal-body")[0]
        document.getElementsByClassName("modal-title")[0].innerHTML = "Who likes"
        whoLikesModalBody.innerHTML = ""    //empty the modal
        let wholikesList = createElement('ul', null, {class:"list-group"})
        whoLikesModalBody.appendChild(wholikesList)
        for (const uid of list){
            getUserInfoById(uid)
            .then((res)=>{
                let wholikesListItem = createElement('li', `${res.username}`, {class:"list-group-item"})
                wholikesList.appendChild(wholikesListItem)
            })
        }
        //clean modal for next use
        for (const i of document.getElementsByClassName('modal-close')){
            i.addEventListener('click',()=>{
                whoLikesModalBody.innerHTML = ""
            })
        }
        //create a worker for polling
    }
    return __showWoLikes
}
function showWhoComment(list, pid, button){
    function __showWhoComment(){
        let whoCommentModalBody = document.getElementsByClassName("modal-body")[0]
        let token = sessionStorage.getItem("token")
        document.getElementsByClassName("modal-title")[0].innerHTML = "Comments"
        console.log(list)
        whoCommentModalBody.innerHTML = "<ul class = \"list-group\">"
        for (const commentsObject of list){
            let author = commentsObject.author
            //let published = new Date(parseFloat(commentsObject.published))
            //console.log(published)
            //let publishedTIme = `${published.getMonth()}${published.getDate()}.${published.getHours()}:${published.getMinutes()}`
            let comment = commentsObject.comment
            whoCommentModalBody.innerHTML += `<li class=\"list-group-item\">${author}: "${comment}"</li>`
        }
        whoCommentModalBody.innerHTML += "</ul>"
        for (const i of document.getElementsByClassName('modal-close')){
            console.log(i)
            i.addEventListener('click',()=>{
                whoCommentModalBody.innerHTML = ""
            })
        }
        if(window.Worker){
            let pollingPostComments = new Worker("./src/pollingPostLikes.js")
            pollingPostComments.onerror = function(e){
                console.log("worker error")
            }
            let workerData = {
                postId: pid,
                userToken: token,
                currentComment: list
            }
            pollingPostComments.postMessage(workerData)
            pollingPostComments.onmessage = function (e){
                let data = e.data
                let moreComment = differComments(workerData.currentComment.concat(data), workerData.currentComment)
                if(moreComment.length != 0){
                    workerData.currentComment = workerData.currentComment.concat(moreComment)
                    for (const i of data){
                        whoCommentModalBody.appendChild(createElement('li', `${i.author}: "${i.comment}"`, {
                            class: "list-group-item"
                        }))
                    }
                    button.innerHTML = workerData.currentComment.length+" comments"
                    pollingPostComments.postMessage(workerData)
                }
            }
        }
    }
    return __showWhoComment
}
function userLikeThisPost(postId){
    function __userLikeThisPost(){
        let userLikeURL = new URL("http://127.0.0.1:5000/post/like")
        let token = sessionStorage.getItem("token")
        console.log(postId.toString())
        userLikeURL.searchParams.append("id", postId.toString())
        fetch(userLikeURL,{
            method: "PUT",
            headers:{
                Authorization: `Token ${token}`,
            }
        })
        .then(()=>{
            alert("Thank you")
        })
    }
    return __userLikeThisPost
}
export function getUserInfoById(id){
    let getUserInfoURL = new URL("http://127.0.0.1:5000/user/")
    console.log(`requiring ${id}'s message`)
    getUserInfoURL.searchParams.append("id", id)
    let token = sessionStorage.getItem("token")
    return fetch(getUserInfoURL,{
        method:"GET",
        headers:{
            Authorization: `Token ${token}`,
        },
    })
    .then((res)=>{
        return res.json()})
    .then((res)=>{
        return res
    })
    .catch((e)=>{
        console.log(e)
    })
}

export function uploadedModal(){
    let uploadedModalBody = document.getElementsByClassName("modal-body")[0]
    document.getElementsByClassName("modal-title")[0].innerHTML = "Upload your picture"
    //create a input button
    //console.log(uploadedModalBody.getElementsByTagName("input"))
    if(uploadedModalBody.getElementsByTagName("input").length == 0 && uploadedModalBody.getElementsByTagName('img').length == 0){
        let uploadButton = createElement("input", "choose a file",{
            type: "file"
        })
        uploadButton.addEventListener("change", uploadImage)
        uploadedModalBody.appendChild(uploadButton)
    }
}


// Given an input element of type=file, grab the data uploaded for use
export function uploadImage(event) {
    const [ file ] = event.target.files;

    const validFileTypes = [ 'image/jpeg', 'image/png', 'image/jpg' ]
    const valid = validFileTypes.find(type => type === file.type);

    // bad data, let's walk away
    if (!valid)
        return false;
    
    // if we get here we have a valid image
    const reader = new FileReader();
    
    reader.onload = (e) => {
        // do something with the data result
        const dataURL = e.target.result;
        const image = createElement('img', null, { src: dataURL, class:"image-in-button", id:"image-to-upload" });
        //button will trigger another modal
        const imageButton = createElement('button', null, {
            class: "button-with-image",
            "data-toggle": "modal",
            "data-target": "ModalForAddDiscription"
            })
        imageButton.appendChild(image)
        //show a the modal which is for add discription
        imageButton.addEventListener("click", ()=>{
            $('#exampleModal').modal('hide')
            $('#ModalForAddDiscription').modal('show')
            document.getElementById("ModalForAddDiscription").getElementsByTagName('textarea')[0].value = image.alt
            let saveChange = document.getElementById("ModalForAddDiscription").getElementsByTagName('button')[2]
            console.log(saveChange.innerHTML)
            saveChange.addEventListener('click', ()=>{
                image.alt = document.getElementById("ModalForAddDiscription").getElementsByTagName('textarea')[0].value
                $('#ModalForAddDiscription').modal('hide')
                //after submit, empty textarea
                // document.getElementById("ModalForAddDiscription").getElementsByTagName('textarea')[0].value = ''
                $('#exampleModal').modal('show')
                console.log(`image's discription is ${image.alt}`)
            })
        })
        document.getElementsByClassName("modal-body")[0].appendChild(imageButton);
        //cannot upload more than one image
        document.getElementsByTagName("input")[0].parentNode.removeChild(document.getElementsByTagName("input")[0])
        //if press "save change in modal, post to back end"
        let saveChangeButton = document.getElementById("saveChange")
        // console.log(document.getElementById('image-to-upload'))
        saveChangeButton.addEventListener("click", uploadNewPost(image))
    };

    // this returns a base64 image
    reader.readAsDataURL(file);
}
//post new uploaded post to backend
function uploadNewPost(img){
    function __uploadNewPost(){
        if(img){
            console.log(img)
            let postURL = new URL("http://127.0.0.1:5000/post/")
            let token = sessionStorage.getItem("token")
            let data = {
                "description_text": `${img.alt}`,
                "src": `${img.src.split(',')[1]}`
            }
            console.log(img.src.split(',')[1])
            fetch(postURL, {
                method:"POST",
                headers: {
                    "Authorization": `Token ${token}`,
                    "Content-Type": "application/json"
                },
                body:JSON.stringify(data)
            })
            .then((res)=>{
                return {
                    status: res.status,
                    post_id: res.json()
                }
            })
            .then((res)=>{
                if(res.status == 200){
                    alert("submission accept. Awosome!")
                    //clean the modal here
                    let uploadedModalBody = document.getElementsByClassName("modal-body")[0]
                    uploadedModalBody.getElementsByTagName('img')[0].parentNode.removeChild(uploadedModalBody.getElementsByTagName('img')[0])
                    // let uploadButton = document.querySelector('input[type="file"]')
                    // uploadButton.value = ""
                    // img = ""
                    $('#exampleModal').modal('hide')
                }
            })
        }        
    }
    return __uploadNewPost

}
/* 
    Reminder about localStorage
    window.localStorage.setItem('AUTH_KEY', someKey);
    window.localStorage.getItem('AUTH_KEY');
    localStorage.clear()
*/
export function checkStore(key) {
    if (window.localStorage)
        return window.localStorage.getItem(key)
    else
        return null

}
{/* <input type="text" class="form-control" aria-label="Default" aria-describedby="inputGroup-sizing-default"></input> */}
function createCommentInputGroup(pid){
    let outerDiv = createElement('div', null, {
        class: "input-group",
        id: "inputComment"     
    })
    let innerDive = createElement('div', null, {
        class: "input-group-prepend"
    })
    let span = createElement('span', "Comment", {
        class: "input-group-text"
    })
    let input = createElement('input', null, {
        class: "form-control",
        "aria-label": "Default",
        "aria-describedby": "inputGroup-sizing-default",
        type: "text"
    })
    outerDiv.appendChild(innerDive)
    outerDiv.appendChild(input)
    innerDive.appendChild(span)
    
    input.addEventListener('change', postComment(pid, input))
    return outerDiv
}
function postComment(pid, input){
    function __postComment(){
        let postCommentURL = new URL("http://127.0.0.1:5000/post/comment")
        postCommentURL.searchParams.append("id", `${pid}`)
        let token = sessionStorage.getItem('token')
        let data = {
            "author": `${sessionStorage.getItem('currentUserName')}`,
            "published": `${Date.now()}`,
            "comment": `${input.value}`
          }
        data = JSON.stringify(data)
        console.log(data)
        fetch(postCommentURL, {
            method:"PUT",
            headers:{
                "Authorization": `Token ${token}`,
                "Content-Type": "application/json"
            },
            body:data
        })
        .then((res)=>{
            console.log(res)
            status = res.status
            if(status == 200){
                alert("Comment Success")
                input.value = ''
            }else{
                alert("Comment fail")
            }
        })
    }
    return __postComment
}
function differComments(commentsList1, commentsList2){
    let different = []
    let sortedList1
    let sortedList2
    if(commentsList1.length > commentsList2.length){
        sortedList1 = commentsList1
        sortedList2 = commentsList2
    }else{
        sortedList1 = commentsList2
        sortedList2 = commentsList1
    }
    for (const i of sortedList1){
        let isInList2 = false
        for(const j of sortedList2){
            if (i.published == j.published){
                isInList2 = true
                break
            }
        }
        if(!isInList2){
            different.push(i)
        }
    }
    return different
}
function showUserPage(username){
    function __getUserInfoByUsername(){

        let getUserInfoURL = new URL("http://127.0.0.1:5000/user/")
        getUserInfoURL.searchParams.append('username', username)
        let token = sessionStorage.getItem('token')
        fetch(getUserInfoURL, {
            method:"GET",
            headers:{
                "Authorization": `Token ${token}`
            }
        })
        .then((res)=>{
            console.log(res.status)
            return res.json()
        })
        .then((res)=>{
            let username = res.username
            let name = res.name
            let email = res.email
            let posts = res.posts.length
            let following = res.following.length
            let followed = `${res.followed_num}`
            let userPageModal = document.getElementById("userPageModal")
            let userPageList = userPageModal.getElementsByTagName('input')
            let userPageModalTitle = document.getElementById('userPageName')
            userPageModalTitle.innerHTML = username
            let userInfo = [name, email, posts, followed, following]
            let i = 0
            while(i < 5){
                console.log(`${userPageList[i]} is ${userInfo[i]}, ${i}`)
                userPageList[i].value = userInfo[i]
                i++
            }
            let postsList = res.posts
            for (const j of postsList){
                getPostByPostId(j)
            }
        })
        let userPageCloseButton = document.getElementById("userPageClose")
        userPageCloseButton.addEventListener('click', ()=>{
            let userPagePosts = document.getElementById('userPagePost')
            while (userPagePosts.firstChild){
                userPagePosts.removeChild(userPagePosts.firstChild)
            }
        })
    }
    return __getUserInfoByUsername
}

function getPostByPostId(pid){
    let postURL = new URL("http://127.0.0.1:5000/post/")
    let token = sessionStorage.getItem('token')
    postURL.searchParams.append('id', `${pid}`)
    fetch(postURL, {
        method: "GET",
        headers:{
            'Content-Type': 'application/json',
            "Authorization": `Token ${token}`
        }
    })
    .then((res)=>{
        if(res.status == 200){
            res.json().then((res)=>{
                let sectionInUserPage = createPostTile(res)
                // for (const i of sectionInUserPage.getElementsByTagName('button')){
                //     i.disabled = true
                // }
                document.getElementById('userPagePost').appendChild(sectionInUserPage)
                
            })
        }else{
            console.log("Get fail")
        }
    })
}