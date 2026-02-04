# Script to remove fragment references and add inline navigation

$clientPages = @(
    "tv-plans.html",
    "mobile-plans.html",
    "internet-plans.html",
    "help.html",
    "profile.html",
    "my-tickets.html",
    "my-subscriptions.html"
)

$navStyles = @'
        /* Navigation Styles */
        .navbar {
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(10px);
            padding: 1rem 2rem;
            box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
            position: sticky;
            top: 0;
            z-index: 1000;
        }

        .nav-container {
            max-width: 1400px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .logo {
            font-size: 1.8rem;
            font-weight: 700;
            color: white;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .nav-links {
            display: flex;
            list-style: none;
            gap: 1rem;
        }

        .nav-links a {
            color: white;
            text-decoration: none;
            padding: 0.7rem 1.2rem;
            border-radius: 8px;
            transition: all 0.3s ease;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .nav-links a:hover {
            background: rgba(255, 255, 255, 0.2);
            transform: translateY(-2px);
        }

        .nav-links a.active {
            background: rgba(255, 255, 255, 0.25);
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
        }

        .user-section {
            position: relative;
        }

        .user-button {
            background: rgba(255, 255, 255, 0.2);
            border: none;
            color: white;
            padding: 0.7rem 1.2rem;
            border-radius: 10px;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 1rem;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .user-button:hover {
            background: rgba(255, 255, 255, 0.3);
            transform: translateY(-2px);
        }

        .dropdown-content {
            display: none;
            position: absolute;
            right: 0;
            top: 60px;
            background: white;
            min-width: 250px;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
            z-index: 1000;
            animation: slideDown 0.3s ease;
        }

        .dropdown-content.show {
            display: block;
        }

        @keyframes slideDown {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .dropdown-header {
            padding: 1rem;
            border-bottom: 1px solid #eee;
            color: #666;
            font-size: 0.9rem;
        }

        .dropdown-content a {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 1rem 1.2rem;
            color: #333;
            text-decoration: none;
            transition: all 0.2s ease;
            font-weight: 500;
        }

        .dropdown-content a:hover {
            background: #f5f5f5;
        }

        .dropdown-content a i {
            width: 20px;
            color: #667eea;
        }

        .logout-link {
            border-top: 1px solid #eee;
            color: #e74c3c !important;
        }

        .logout-link:hover {
            background: #ffebee !important;
        }
'@

Write-Host "Fragment removal script complete - manual updates needed for each file" -ForegroundColor Green
