use tauri::Manager;

mod commands;
mod error;

pub use error::Error;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tracing_subscriber::fmt()
        .with_env_filter(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "maximeze=debug".into()),
        )
        .init();

    tauri::Builder::default()
        .plugin(tauri_plugin_shell::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_store::Builder::new().build())
        .invoke_handler(tauri::generate_handler![
            commands::open_url,
            commands::get_page_title,
            commands::block_request,
            commands::get_app_version,
        ])
        .setup(|app| {
            let window = app.get_webview_window("main").unwrap();

            // Remove default title bar for custom chrome
            #[cfg(target_os = "windows")]
            {
                window.set_decorations(false).ok();
            }

            tracing::info!("Maximeze browser started");
            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
