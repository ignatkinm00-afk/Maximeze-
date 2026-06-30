use serde::Serialize;
use thiserror::Error;

#[derive(Debug, Error, Serialize)]
pub enum Error {
    #[error("IO error: {0}")]
    Io(String),

    #[error("Navigation error: {0}")]
    Navigation(String),

    #[error("Store error: {0}")]
    Store(String),
}

impl From<std::io::Error> for Error {
    fn from(e: std::io::Error) -> Self {
        Self::Io(e.to_string())
    }
}
