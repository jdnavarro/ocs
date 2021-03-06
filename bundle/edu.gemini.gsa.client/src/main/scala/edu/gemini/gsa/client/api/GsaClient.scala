package edu.gemini.gsa.client.api

/**
 * Provides support for querying the GSA for datasets.
 */
trait GsaClient {
  /**
   * Query the GSA for datasets that match the provided parameters
   */
  def query(params: GsaParams): GsaResult
}