function FindProxyForURL(url, host) { 
  // Send to proxy 
  if ( isPlainHostName("www.cgchannel.com") || shExpMatch(host, "*myip.com") || shExpMatch(host, "*whatismyipaddress.com") || shExpMatch(host, "management-aviatrix.infra.backbase.cloud") || shExpMatch(host, "*.backbasecloud.com") ) { 
    return "PROXY webproxy.infra.backbase.cloud:8888;"; 
  }; 
  // Send to legacy proxy 
  if ( shExpMatch(host, "*.dev.wu.live.backbaseservices.com") || shExpMatch(host, "*.fnis.com") || shExpMatch(host, "*.dev.pl.live.backbaseservices.com") || shExpMatch(host, "*.stg.bnp.live.backbaseservices.com") || shExpMatch(host, "*.dev.bnp.live.backbaseservices.com") || shExpMatch(host, "*whatsmyip.org") || shExpMatch(host, "*.dev.fmb.live.backbaseservices.com") || dnsDomainIs(host, ".live.backbaseservices.com") ) { 
    return "PROXY legacy-webproxy.backbase.cloud:3128;"; 
  }; 
  // If we made it here. go direct 
  return "DIRECT;"; };
