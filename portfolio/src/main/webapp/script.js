// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

const GOOGLE_API_KEY = "AIzaSyCNzSV2oZJ6L1faY_mnZUZp2DA9JQTQ6kU";

// true represents that the blinking text is hidden, false shows
let alternateOnOff = false;

// Function to be run on index page load. Calls all initial functions.
function init() {
  typeWriterEffect(0, 0);
  displayServletContent(5);
  initializeMap();
  checkAuth();
}

// Interval for repeated blinking text effect on page
setInterval(() => {
  if (alternateOnOff) {
    document.getElementById("blinking-text").style.opacity = 0;
  } else {
    document.getElementById("blinking-text").style.opacity = 1;
  }
  alternateOnOff = !alternateOnOff;
}, 500);

function scrollToProjects() {
  document.getElementById("projects").scrollIntoView({ behavior: "smooth" });
}

/**
 * Recursive function that fades out the tw-text div gradually over one second.
 * @param {number} currOpacity
 */
function fadeOutText(currOpacity) {
  setTimeout(() => {
    document.getElementById("tw-text").style.opacity = currOpacity;
    if (currOpacity > 0) {
      fadeOutText(currOpacity - 0.1);
    }
  }, 100);
}

/**
 * Typewriter effect loop that applies the effect to each string in the facts
 * array. Takes the current char index being written and the index of the current fact
 * in the facts array.
 * @param {number} charIndex @param {number} currentFactIndex
 */
function typeWriterEffect(charIndex, currentFactIndex) {
  const facts = [
    "I'm a rising sophomore at Cornell University.",
    "I like making things that make a marked improvement on our lives.",
    "I'm an avid badminton player!",
    "My Chinese name is Wu Lingrui (吴灵锐).",
  ];
  const currentFact = facts[currentFactIndex];

  if (charIndex < currentFact.length) {
    document.getElementById("tw-text").innerHTML += currentFact.charAt(
      charIndex
    );
    setTimeout(() => typeWriterEffect(charIndex + 1, currentFactIndex), 45);
  } else {
    fadeOutText(1);
    setTimeout(() => {
      document.getElementById("tw-text").innerHTML = "&nbsp;";
      setTimeout(() => {
        //reset fade out
        document.getElementById("tw-text").style.opacity = 1;
        typeWriterEffect(0, (currentFactIndex + 1) % facts.length);
      }, 55);
    }, 2500);
  }
}

/**
 * Sorts the comment array according to the current value of the select
 * dropdown menu
 * @param {Array} commentObjArray contains objects with username, text,
 * and timestamp fields
 */
function sortCommentArr(commentObjArray) {
  const currentSortOption = document.getElementById("comment-sort-menu").value;
  switch (currentSortOption) {
    case "descending-time":
      commentObjArray.sort((a, b) => b.timestamp - a.timestamp);
      break;
    case "ascending-time":
      commentObjArray.sort((a, b) => a.timestamp - b.timestamp);
      break;
    case "descending-len":
      commentObjArray.sort((a, b) => b.text.length - a.text.length);
      break;
    case "ascending-len":
      commentObjArray.sort((a, b) => a.text.length - b.text.length);
      break;
    case "scramble":
      commentObjArray.sort(() => Math.random() - 0.5);
      break;
  }
}

/**
 * Renders the plaintext as a result of calling the /data GET endpoint by
 * setting the `comments-container` div to the text with certain number of
 * comments list. Throws an error if the endpoint does not return a JSON
 * array.
 * @param {number} numCommentsToShow. If === -1, then show all comments.
 */
async function displayServletContent(numCommentsToShow) {
  const res =
    numCommentsToShow === -1
      ? await fetch("/data")
      : await fetch(`/data?limit=${numCommentsToShow}`);
  const json = await res.json();
  if (!Array.isArray(json)) {
    throw new Error("Response data is not an array");
  }

  if (json.length === 0) {
    document.getElementById("comments-section").innerHTML =
      "<h5>No comments to show.</h5>";
    document.getElementById("comment-delete-button").style.display = "none";
  } else {
    sortCommentArr(json);
    document.getElementById("comments-section").innerHTML = "";
    json.map(({ name, text, timestamp }, index) => {
      document.getElementById("comments-section").innerHTML += `
            <div class="comments-card">
              <div class="comments-card-header">
                <img
                  src="/assets/images/account_circle-24px.svg"
                  alt="Icon of comment user"
                  class="comments-icon"
                />
                <span class="comments-card-user" id="${index}-username">
                </span>
              </div>
              <p class="card-text" id="${index}-text"></p>
            </div>
          `;
      document.getElementById(
        `${index}-username`
      ).textContent = `${name} · ${new Date(timestamp).toLocaleDateString()}`;
      document.getElementById(`${index}-text`).textContent = `${text}`;
    });

    document.getElementById("comment-delete-button").style.display = "block";
  }
}

/**
 * Displays servlet comments at a limit determined by the parameter string.
 * @param {string} value - the option of the select dropdown for comment limit numbers.
 */
function displayServletContentUsingString(value) {
  switch (value) {
    case "5":
      displayServletContent(5);
      break;
    case "10":
      displayServletContent(10);
      break;
    case "all":
      displayServletContent(-1);
      break;
    case "none":
      displayServletContent(0);
      break;
    default:
      throw new Error("Unimplemented # comments encountered");
  }
}

function rerenderCommentsWithCurrentLimit() {
  const limit = document.getElementById("comment-number-shown").value;
  displayServletContentUsingString(limit);
}

// Listen for changes in comment number selected and rerender comments section
// as needed. Throws an error if cases for 5, 10, all, or none are not
// encountered.
const selected = document.querySelector("#comment-number-shown");
selected.addEventListener("change", (event) => {
  displayServletContentUsingString(event.target.value);
});

// Listen for changes in sort method and rerender comments as needed.
const sortMenuElement = document.querySelector("#comment-sort-menu");
sortMenuElement.addEventListener("change", (event) => {
  rerenderCommentsWithCurrentLimit();
});

// Prevent comment form submit button from automatically refreshing upon click
const formElement = document.getElementById("comments-form");
formElement.addEventListener("submit", (event) => {
  event.preventDefault();
});

// Prevent Marker creation form button from automatically going to the action URI
const checkAddressButton = document.getElementById("check-address-button");
checkAddressButton.addEventListener("click", (event) => {
  event.preventDefault();
});

/**
 * POST request to the /data endpoint with query params for username and text
 * corresponding to the form fields; alerts if response code is not 2xx
 */
function addComment() {
  const username = document.getElementById("comment-username").value;
  const text = document.getElementById("comment-input").value;
  fetch(`/data?username=${username}&text=${text}`, { method: "POST" }).then(
    (res) => {
      if (res.ok) {
        rerenderCommentsWithCurrentLimit();
        document.getElementById("comment-username").value = "";
        document.getElementById("comment-input").value = "";
      } else {
        alert("Error: Enter a valid comment.");
      }
    }
  );
}

async function deleteComments() {
  await fetch("/delete-data", { method: "POST" });
  displayServletContent(0);
}

const CORNELL_COORDS = { lat: 42.449, lng: -76.474 };

const DEFAULT_MARKERS = [
  {
    lat: 42.455,
    lng: -76.4777,
    title: "Mary Donlon Hall: My Dorm",
    content: `I lived in Donlon Hall during my freshman year. It was a good time,
    since it is considered the "social" dorm.`,
    image: "donlon.jpg",
  },
  {
    lat: 42.4486,
    lng: -76.4863,
    title: "The Slope",
    content: `Libe Slope ("The Slope") is one of the best views at Cornell. It
    faces west, so you can see a nice view of the sunset, but it is still pretty
    annoying to walk up every day, so I try my best to avoid it.`,
    image: "libe.jpg",
  },
  {
    lat: 42.4498,
    lng: -76.4816,
    title: "PSB: Best Natural Light",
    content: `The Physical Sciences Building (PSB) is the home of physics majors
    at Cornell, but is a popular study spot among engineers. Contains a nice cafe
    called Goldie's and very nice natural light that keeps you from falling 
    asleep.`,
    image: "psb.jpg",
  },
  {
    lat: 42.4479,
    lng: -76.4793,
    title: "Trillium: Best Food Spot",
    content: `Trillium is easily the best place to get lunch on campus. It has
    many options for food, but does not take meal swipes: it is an eatery rather
    than a dining hall, so you can either use credit card or Big Red Bucks (Cornell's
    version of dining dollars which are untaxed)`,
    image: "trillium.jpg",
  },
  {
    lat: 42.445,
    lng: -76.4812,
    title: "Gates Hall: Home of CS",
    content: `The Bill & Melinda Gates Hall is the home of CIS, or Computing and
    Information Science. Some upper-level CS courses are taught here, and this
    is where all the PhDs and professors have their offices. A pretty futuristic
    building, it was built in 2014.`,
    image: "gates.jpg",
  },
  {
    lat: 42.4529,
    lng: -76.4774,
    title: "Helen Newman: Where I Play Badminton",
    content: `Helen Newman Hall is a fitness center with gyms and other fitness
    equipment (volleyball and badminton nets). The badminton team practices here
    every Tuesday, Thursday, and Saturday (though I miss a lot of the practices
    due to work/studies).`,
    image: "helennewman.jpg",
  },
];

/**
 * Renders all the markers in a certain array of marker objects to a certain
 * map.
 * @param {Array} markerArray which must have keys lat, lng, title, content,
 * and optionally, image.
 * @param {google.maps.Map} map
 */
function renderMarkersToMap(markerArray, map) {
  markerArray.map((obj) => {
    const marker = new google.maps.Marker({
      position: { lat: obj.lat, lng: obj.lng },
      map: map,
      title: obj.title,
    });
    const infoWindow = new google.maps.InfoWindow({
      content:
        `<h1 class="infowindow-text">${obj.title}</h1>` +
        (!obj.hasOwnProperty("image")
          ? ``
          : `<img src="assets/images/${obj.image}" class="infowindow-img" alt="${obj.title}" />`) +
        `<p class="infowindow-text">${obj.content}</p>`,
    });
    marker.addListener("click", () => infoWindow.open(map, marker));
  });
}

/**
 * Adds markers in the markers const array and those stored in the Datastore
 * (accessible via /map-marker endpoint) to a certain Google Map
 * @param {google.maps.Map} map - Map object for which the markers will be added
 */
async function initializeMarkers(map) {
  renderMarkersToMap(DEFAULT_MARKERS, map);
  const markerResponse = await fetch("/map-marker");
  const userCreatedMarkers = await markerResponse.json();
  if (!Array.isArray(userCreatedMarkers)) {
    throw new Error("Response data is not an array");
  }
  renderMarkersToMap(userCreatedMarkers, map);
}

/**
 * Creates a map of Cornell University and adds it to the page
 */
function initializeMap() {
  const { lat, lng } = CORNELL_COORDS;
  const map = new google.maps.Map(document.getElementById("map"), {
    center: { lat: lat, lng: lng },
    zoom: 15,
  });
  initializeMarkers(map);
}

/**
 * Retrieves latitude and longitude values from respective fields and queries
 * the Google Geocoding API for an approximate address string. If an error occurs
 * in the API fetch or bad input is provided, error message is alerted.
 */
async function getAddress() {
  const lat = document.getElementById("lat").value;
  const lng = document.getElementById("lng").value;
  if (lat.length === 0 || lng.length === 0) {
    alert("Enter a valid latitude/longitude pair!");
  } else {
    const mapsAPIResponse = await fetch(
      `https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lng}&key=${GOOGLE_API_KEY}`
    );
    const json = await mapsAPIResponse.json();
    if (mapsAPIResponse.ok) {
      const address = json.results[0].formatted_address;
      if (json.results !== undefined && address !== undefined) {
        document.getElementById("address-display").innerText = address;
      } else {
        document.getElementById("address-display").innerText =
          "No nearby address found";
      }
    } else {
      alert(`Error! ${json.error_message}`);
    }
  }
}

/**
 * Fetches auth status of client from backend and renders corresponding
 * status to the DOM, with a login/logout URL.
 */
async function checkAuth() {
  const response = await fetch("/auth-status");
  const { isLoggedIn, email, loginUrl, logoutUrl } = await response.json();

  if (isLoggedIn) {
    document.getElementById(
      "login-greeting"
    ).innerHTML = `<p>You are logged in as ${email}. <a href="${logoutUrl}">Logout</a></p>`;
  } else {
    document.getElementById(
      "comments-form-div"
    ).innerHTML = `<p class="login-text">Login to post a comment <a href="${loginUrl}">here</a>.</p>`;
  }
}
