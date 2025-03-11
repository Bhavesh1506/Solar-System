import React, { useState, useEffect } from "react";
import "./App.css";

function App() {
  const [selectedPlanet, setSelectedPlanet] = useState(null);

  // Planet data
  const planets = [
    {
      id: "mercury",
      name: "Mercury",
      color: "#A9A9A9",
      size: 4.8,
      distance: 36,
      orbitSpeed: 1.6,
      facts: [
        "Smallest planet in our solar system",
        "Closest planet to the Sun",
        "No atmosphere to retain heat",
        "Completes an orbit every 88 Earth days",
      ],
    },
    {
      id: "venus",
      name: "Venus",
      color: "#E6BE8A",
      size: 12,
      distance: 67,
      orbitSpeed: 1.17,
      facts: [
        "Similar in size to Earth",
        "Hottest planet with surface temperatures around 900°F",
        "Dense atmosphere of carbon dioxide",
        "Rotates backward compared to other planets",
      ],
    },
    {
      id: "earth",
      name: "Earth",
      color: "#4B70DD",
      size: 12.5,
      distance: 93,
      orbitSpeed: 1.0,
      facts: [
        "Only known planet to support life",
        "Approximately 71% of surface is covered with water",
        "Has one natural satellite: the Moon",
        "Has an atmosphere rich in nitrogen and oxygen",
      ],
    },
    {
      id: "mars",
      name: "Mars",
      color: "#E27B58",
      size: 6.7,
      distance: 142,
      orbitSpeed: 0.8,
      facts: [
        "Known as the Red Planet due to iron oxide in soil",
        "Has two small moons: Phobos and Deimos",
        "Features the largest volcano in the solar system: Olympus Mons",
        "Evidence suggests Mars once had liquid water on its surface",
      ],
    },
    {
      id: "jupiter",
      name: "Jupiter",
      color: "#E8B36B",
      size: 30,
      distance: 220,
      orbitSpeed: 0.43,
      facts: [
        "Largest planet in our solar system",
        "A gas giant primarily composed of hydrogen and helium",
        "The Great Red Spot is a giant storm that has lasted for hundreds of years",
        "Has at least 79 moons",
      ],
    },
    {
      id: "saturn",
      name: "Saturn",
      color: "#EAD196",
      size: 25,
      distance: 270,
      orbitSpeed: 0.32,
      facts: [
        "Known for its extensive ring system",
        "Second largest planet in our solar system",
        "Has at least 82 moons with Titan being the largest",
        "Lowest density of all planets – it would float in water",
      ],
    },
    {
      id: "uranus",
      name: "Uranus",
      color: "#CAECEC",
      size: 15,
      distance: 320,
      orbitSpeed: 0.22,
      facts: [
        "Rotates on its side with an axial tilt of about 98 degrees",
        "Ice giant composed primarily of water, methane, and ammonia ices",
        "Has 13 known rings and 27 known moons",
        "First planet discovered using a telescope",
      ],
    },
    {
      id: "neptune",
      name: "Neptune",
      color: "#5B5BFF",
      size: 14,
      distance: 370,
      orbitSpeed: 0.18,
      facts: [
        "Windiest planet with speeds reaching 1,200 mph",
        "Most distant planet in our solar system",
        "Has 14 known moons with Triton being the largest",
        "Great Dark Spot is a storm similar to Jupiter's Great Red Spot",
      ],
    },
  ];

  // Animation
  useEffect(() => {
    const interval = setInterval(() => {
      // Force re-render to update planet positions
      setSelectedPlanet((prev) => (prev ? { ...prev } : null));
    }, 50);

    return () => clearInterval(interval);
  }, []);

  // Planet component
  const Planet = ({ planet, isSelected, onClick }) => {
    const orbitSize = planet.distance;

    return (
      <>
        {/* Orbit circle */}
        <div
          style={{
            border: "1px solid rgba(255, 255, 255, 0.1)",
            borderRadius: "50%",
            width: `${orbitSize * 2}px`,
            height: `${orbitSize * 2}px`,
            position: "absolute",
            left: "50%",
            top: "50%",
            transform: "translate(-50%, -50%)",
            zIndex: 0,
          }}
        />

        {/* Planet */}
        <div
          onClick={() => onClick(planet)}
          style={{
            backgroundColor: planet.color,
            width: `${planet.size}px`,
            height: `${planet.size}px`,
            borderRadius: "50%",
            position: "absolute",
            cursor: "pointer",
            left: `calc(50% + ${
              Math.cos(Date.now() * 0.0001 * planet.orbitSpeed) * orbitSize
            }px)`,
            top: `calc(50% + ${
              Math.sin(Date.now() * 0.0001 * planet.orbitSpeed) * orbitSize
            }px)`,
            transform: "translate(-50%, -50%)",
            transition: "background-color 0.3s",
            boxShadow: isSelected
              ? "0 0 10px 5px rgba(255, 255, 255, 0.5)"
              : "none",
            zIndex: isSelected ? 10 : 1,
          }}
        />
      </>
    );
  };

  // Sun component
  const Sun = () => (
    <div
      style={{
        width: "30px",
        height: "30px",
        backgroundColor: "yellow",
        borderRadius: "50%",
        position: "absolute",
        left: "50%",
        top: "50%",
        transform: "translate(-50%, -50%)",
        boxShadow: "0 0 30px 15px rgba(255, 165, 0, 0.5)",
        zIndex: 2,
      }}
    />
  );

  return (
    <div
      style={{
        width: "100%",
        height: "100vh",
        backgroundColor: "black",
        overflow: "hidden",
        position: "relative",
        fontFamily: "Arial, sans-serif",
      }}
    >
      <div
        style={{
          padding: "20px",
          backgroundColor: "#111",
          color: "white",
          borderBottom: "1px solid #333",
        }}
      >
        <h1 style={{ fontSize: "24px", fontWeight: "bold", margin: 0 }}>
          Space Exploration Dashboard
        </h1>
        <p style={{ color: "#aaa", margin: "5px 0 0 0" }}>
          Explore our solar system
        </p>
      </div>

      <div style={{ position: "relative", height: "calc(100vh - 80px)" }}>
        <Sun />

        {planets.map((planet) => (
          <Planet
            key={planet.id}
            planet={planet}
            isSelected={selectedPlanet?.id === planet.id}
            onClick={setSelectedPlanet}
          />
        ))}

        {selectedPlanet && (
          <div
            style={{
              position: "absolute",
              bottom: 0,
              left: 0,
              right: 0,
              backgroundColor: "rgba(20,20,30,0.9)",
              color: "white",
              padding: "20px",
              transition: "all 0.3s ease",
            }}
          >
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
            >
              <h2 style={{ fontSize: "28px", margin: "0 0 15px 0" }}>
                {selectedPlanet.name}
              </h2>
              <button
                style={{
                  background: "none",
                  border: "1px solid #666",
                  color: "white",
                  padding: "5px 10px",
                  borderRadius: "4px",
                  cursor: "pointer",
                }}
                onClick={() => setSelectedPlanet(null)}
              >
                Close
              </button>
            </div>

            <div style={{ display: "flex", gap: "40px" }}>
              <div>
                <h3
                  style={{
                    fontSize: "18px",
                    marginBottom: "10px",
                    color: "#ccc",
                  }}
                >
                  Key Facts
                </h3>
                <ul style={{ paddingLeft: "20px", margin: 0 }}>
                  {selectedPlanet.facts.map((fact, index) => (
                    <li key={index} style={{ marginBottom: "8px" }}>
                      {fact}
                    </li>
                  ))}
                </ul>
              </div>

              <div>
                <h3
                  style={{
                    fontSize: "18px",
                    marginBottom: "10px",
                    color: "#ccc",
                  }}
                >
                  Planet Data
                </h3>
                <p>
                  <strong>Distance from Sun:</strong> {selectedPlanet.distance}{" "}
                  million km
                </p>
                <p>
                  <strong>Orbital Period:</strong>{" "}
                  {Math.round(365 / selectedPlanet.orbitSpeed)} Earth days
                </p>
                <p>
                  <strong>Diameter:</strong> {selectedPlanet.size * 1000} km
                  (scaled)
                </p>
              </div>
            </div>
          </div>
        )}
      </div>

      <div
        style={{
          position: "absolute",
          bottom: selectedPlanet ? "220px" : "20px",
          right: "20px",
          color: "#aaa",
          fontSize: "14px",
          textAlign: "right",
          transition: "bottom 0.3s",
        }}
      >
        <p>Click on a planet to learn more</p>
      </div>
    </div>
  );
}

export default App;
