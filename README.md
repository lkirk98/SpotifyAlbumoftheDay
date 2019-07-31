# SpotifyAlbumoftheDay
This app looks at all the songs that are saved in your Spotify library and retrieves all the albums they are from. Then, it chooses one of these albums randomly to populate a playlist.

It uses both the <a href = "https://github.com/spotify/android-auth/releases">Spotify Android Authorization Library</a> and <a href = "https://github.com/kaaes/spotify-web-api-android">kaaes' Android Spotify Web Wrapper</a>.

<h2>TODO</h2>
<ul>
  <li>Integrate reasonable UI</li>
  <li>Automatically populate the Album of the Day playlist</li>
  <li>Let user have more fine grained control of which albums they want used in this process, and also let them have more options on where they come from (ie songs from certain playlists, etc)</li>
  <li>Let user choose when and how often the playlist is changed</li>
  <li>Store user's albums, so the entire algorithm does not need to run every time</li>
 </ul>
