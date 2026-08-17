/**
 * Deployment: the API is served from the same host, behind /api and /uploads,
 * so an empty base means "same origin". Nothing has to know the domain name,
 * which is what lets the same image run on any host.
 */
export const environment = {
  apiBaseUrl: '',
};
