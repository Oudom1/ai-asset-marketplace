package com.oudom.marketplace;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*")
public class AssetController {

    record Asset(Long id, String title, String category, String image, double price, String creator) {}

    @GetMapping
    public List<Asset> all() {
        return List.of(
            new Asset(1L, "Analytics Dashboard UI", "Dashboard", "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80", 12.00, "PixelForge"),
            new Asset(2L, "Finance Admin Dashboard", "UI Design", "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=1200&q=80", 18.00, "NovaStudio"),
            new Asset(3L, "Dark Metrics Dashboard", "Data Analysis", "https://images.unsplash.com/photo-1556155092-490a1ba16284?auto=format&fit=crop&w=1200&q=80", 15.00, "MetricLab"),
            new Asset(4L, "Purple CRM Dashboard", "Project Management", "https://images.unsplash.com/photo-1543286386-713bdd548da4?auto=format&fit=crop&w=1200&q=80", 20.00, "AsterUI"),
            new Asset(5L, "Sales Overview Template", "Business Growth", "https://images.unsplash.com/photo-1531297484001-80022131f5a1?auto=format&fit=crop&w=1200&q=80", 9.00, "DashWorks"),
            new Asset(6L, "AI Operations Console", "AI", "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80", 25.00, "AgenticLab")
        );
    }
}
