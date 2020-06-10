const USA_COORDS = { lat: 37.199, lng: -98.5795 };

function init() {
  initializeCOVIDMap();
}

let min = Number.MAX_VALUE;
let max = -Number.MAX_VALUE;

/**
 * Fetches data from the COVID-19 API on confirmed cases per U.S.
 * state/territory and sets the data as a property on each state/territory.
 * @param {google.maps.Map} map 
 */
async function renderCOVIDData(map) {
  const data = await fetch("https://api.covid19api.com/live/country/us");
  const json = await data.json();

  json.forEach(({ Confirmed, Province }) => {
    if (map.data.getFeatureById(Province) !== undefined) {
      min = Confirmed < min ? Confirmed : min;
      max = Confirmed > max ? Confirmed : max;
      map.data.getFeatureById(Province).setProperty("cases", Confirmed);
    }
  });
  document.getElementById("min").textContent = min.toLocaleString();
  document.getElementById("max").textContent = max.toLocaleString();
}

/**
 * Returns the styling associated with a certain feature representing a
 * state/territory, based on its "cases" property.
 * @param {google.maps.Data.Feature} feature
 */
function styleCallback(feature) {
  const low = [5, 100, 86];
  const high = [5, 100, 41];

  const delta = (feature.getProperty("cases") - min) / (max - min);
  const color = [];
  for (let i = 0; i < 3; i++) {
    color[i] = (high[i] - low[i]) * delta + low[i];
  }

  return {
    strokeWeight: 1.5,
    strokeColor: "#000",
    fillColor: "hsl(" + color[0] + "," + color[1] + "%," + color[2] + "%)",
    fillOpacity: 0.8,
  };
}

/**
 * Creates a map of the contiguous US with boundaries by state and adds it to the page
 */
async function initializeCOVIDMap() {
  const { lat, lng } = USA_COORDS;
  const map = new google.maps.Map(document.getElementById("map-covid"), {
    center: { lat: lat, lng: lng },
    zoom: 4.9,
  });
  map.data.setStyle(styleCallback);

  await map.data.loadGeoJson(
    "https://storage.googleapis.com/mapsdevsite/json/states.js",
    { idPropertyName: "NAME" }
  );
  google.maps.event.trigger(document.getElementById("cases"), "change");
  renderCOVIDData(map);
}
