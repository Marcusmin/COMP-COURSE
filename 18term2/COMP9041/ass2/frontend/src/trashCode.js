//create a login form
// function createLoginForm(){
//     // return fetch("../borrow_code/login.html")
//     // .then(res => res.text())
//     // .then((res) =>{
//         let parser = new DOMParser()    //Copy from stackoverflow
//         let loginFormDom = parser.parseFromString(res, "text/html")
//         let loginFormContainer = loginFormDom.getElementById("loginFormContainer")
//         loginFormContainer.style.position = "fixed"
//         loginFormContainer.style.width = "100%"
//         loginFormContainer.style.margin = "0 auto"
//         let loginFormCSS = createElement('link', '',{"id": "loginStyle", "type": "text/css", "rel": "styleSheet", "href": "../styles/loginFormStyle.css"})
//         let header = document.getElementsByTagName("header")[0]
//         header.appendChild(loginFormCSS)
//         let mainrPage = document.getElementsByTagName('main')[0];
//         let pageBody = document.getElementsByTagName("body")[0]
//         pageBody.insertBefore(loginFormContainer, mainrPage)
//         let closeButton = document.getElementById("closeButton")
//         closeButton.addEventListener("click", removeLoginForm)
//         hasCreateLoginForm = true
//         console.log("Create a login form")
//     // })
// }
//remove login form
// function removeLoginForm(){
//     let loginFormContainer = document.getElementById("loginFormContainer")
//     loginFormContainer.parentNode.removeChild(loginFormContainer)
//     //remove the style of login form
//     let loginstyle = document.getElementById("loginStyle")
//     loginstyle.parentNode.removeChild(loginstyle)
//     hasCreateLoginForm = false
//     console.log("login form removed")
// }
//show logout button
//create registration form
// function createRegistrationForm(){
//     return fetch("../borrow_code/register.html")
//     .then(res => res.text())
//     .then((res) => {
//         let parser = new DOMParser()    //Copy from stackoverflow
//         let registrationDom = parser.parseFromString(res, "text/html")
//         let registrationContainer = registrationDom.getElementById("registerContainer")
//         registrationContainer.style.position = "fixed"
//         registrationContainer.style.width = "100%"
//         registrationContainer.style.margin = "0 auto"
//         let mainrPage = document.getElementsByTagName('main')[0];
//         let pageBody = document.getElementsByTagName("body")[0]
//         pageBody.insertBefore(registrationContainer, mainrPage)
//         let registrationFormCSS = createElement('link', '',{"id": "registrationStyle", "type": "text/css", "rel": "styleSheet", "href": "../styles/registerFormCSS.css"})
//         let main = document.getElementsByTagName("main")[0]
//         main.appendChild(registrationFormCSS)
//         let closeButton = document.getElementById("closeButton")
//         closeButton.addEventListener("click", removeRegistrationForm)
//         hasCreateRegistrationForm = true
//     })
// }
// function removeRegistrationForm(){
//     let form = document.getElementById("registerContainer")
//     form.parentNode.removeChild(form)
//     let registrationStyle =  document.getElementById("registrationStyle")
//     registrationStyle.parentNode.removeChild(registrationStyle)
//     hasCreateRegistrationForm = false
// }