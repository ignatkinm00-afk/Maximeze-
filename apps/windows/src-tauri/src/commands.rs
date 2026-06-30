use crate::Error;

/// Opens a URL in the system default browser (for external links)
#[tauri::command]
pub async fn open_url(url: String) -> Result<(), Error> {
    // Validate URL before opening
    if !url.starts_with("http://") && !url.starts_with("https://") {
        return Err(Error::Navigation(format!("Disallowed URL scheme: {}", url)));
    }
    tracing::debug!("Opening external URL: {}", url);
    Ok(())
}

/// Returns the page title for a given URL (fetched by the WebView, passed back via event)
#[tauri::command]
pub async fn get_page_title(url: String) -> Result<String, Error> {
    // In production this is filled by the WebView navigation event
    Ok(url)
}

/// Checks whether a network request should be blocked (tracker/ad blocking)
#[tauri::command]
pub async fn block_request(url: String, filters_enabled: bool) -> Result<bool, Error> {
    if !filters_enabled {
        return Ok(false);
    }

    // Basic heuristic blocker — production implementation uses EasyList/EasyPrivacy lists
    let blocked_domains = [
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "amazon-adsystem.com",
        "facebook.com/tr",
        "analytics.google.com",
        "google-analytics.com",
        "hotjar.com",
        "mixpanel.com",
        "segment.com",
    ];

    let should_block = blocked_domains.iter().any(|domain| url.contains(domain));
    if should_block {
        tracing::debug!("Blocked request: {}", url);
    }
    Ok(should_block)
}

/// Returns the current app version
#[tauri::command]
pub fn get_app_version() -> String {
    env!("CARGO_PKG_VERSION").to_string()
}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_block_request_blocks_trackers() {
        let blocked = block_request("https://www.google-analytics.com/collect".into(), true)
            .await
            .unwrap();
        assert!(blocked);
    }

    #[tokio::test]
    async fn test_block_request_allows_normal_urls() {
        let blocked = block_request("https://example.com/page".into(), true)
            .await
            .unwrap();
        assert!(!blocked);
    }

    #[tokio::test]
    async fn test_block_request_disabled() {
        let blocked = block_request("https://doubleclick.net/ad".into(), false)
            .await
            .unwrap();
        assert!(!blocked);
    }

    #[tokio::test]
    async fn test_open_url_rejects_non_http() {
        let result = open_url("javascript:alert(1)".into()).await;
        assert!(result.is_err());
    }

    #[test]
    fn test_get_app_version() {
        let version = get_app_version();
        assert!(!version.is_empty());
    }
}
