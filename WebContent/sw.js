var version="v1.0.5";
var cacheName = 'CSV Parser:'+version;  
var filesToCache = [
	'js/angular-route.min.js',
	'js/angular.min.js',
	'js/jquery.min.js',
	'js/materialize.min.js',
	'css/materialize.min.css',
	'partials/createTable.html',
	'partials/viewTable.html',
	'js/app.controller.js',
	'js/app.module.js',
	'index.html'
];
//cache static resources
self.addEventListener('install', function(ev) {  
  	console.log('[ServiceWorker] Install');   
  	ev.waitUntil(caches
      .open(cacheName)
      .then(function(cache) {
      	console.log('[ServiceWorker] Caching files');
        return cache.addAll(filesToCache);
      })
      .then(function(){
    	  /**
    	  This code will prevent the service worker from waiting until all of the currently open tabs on your site are closed before it becomes active
    	  **/
    	  self.skipWaiting();
      })
    ); 
});
//Update cache on updating sw
self.addEventListener('activate', function(e){
  console.log('[ServiceWorker] Activate');
  e.waitUntil(
    caches.keys().then(function(keyList){
      return Promise.all(keyList.map(function(key){
        console.log('[ServiceWorker] Removing old cache', key);
        if (key != cacheName) {
          return caches.delete(key);
        }
      }));
    })
    .then(function(){
    	//this code will cause the new service worker to take over responsibility for the still open pages.
    	self.clients.claim();
    })
  );
});
//return cache on fetch
self.addEventListener('fetch', function(e){
  e.respondWith(
    caches.match(e.request).then(function(response) {
    	if(response){
    		console.info("Fulfilling "+e.request.url+" from cache.");
    	}
      	return response || fetch(e.request);
    })
  );
});

