const USA_COORDS = { lat: 38.599, lng: -98.5795 };

function init() {
  initializeCOVIDMap();
}

/**
 * Creates a map of the contiguous US and adds it to the page
 */
function initializeCOVIDMap() {
  const { lat, lng } = USA_COORDS;
  const map = new google.maps.Map(document.getElementById("map-covid"), {
    center: { lat: lat, lng: lng },
    zoom: 5,
  });
}
