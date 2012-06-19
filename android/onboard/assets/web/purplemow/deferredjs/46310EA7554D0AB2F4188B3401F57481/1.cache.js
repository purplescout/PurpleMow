function qh(){}
function yh(){}
function Ah(){}
function xh(){}
function Dh(){}
function Ch(){}
function Ph(){}
function Yh(){}
function ai(){}
function ei(){}
function hi(){}
function ki(){}
function Wo(){}
function Vo(){}
function RA(){}
function OA(){}
function XA(){}
function Rt(){ke()}
function bi(a){this.b=a}
function ZA(a){this.b=a}
function YA(a,b){a.b.R(b)}
function cp(a){$wnd.alert(a)}
function Jh(a){$wnd.clearTimeout(a)}
function Ih(a){$wnd.clearInterval(a)}
function fi(a){xc.call(this,a)}
function Zh(a,b){this.c=a;this.b=b}
function Nh(a,b){Fh();this.b=a;this.c=b}
function Vh(a){Rh();Wh.call(this,!a?null:a.b)}
function Fh(){Fh=gC;Eh=new fx;ap(new Wo)}
function Gh(a){a.d?Ih(a.e):Jh(a.e);ex(Eh,a)}
function Th(a,b,c){oi('callback',c);return Sh(a,b,c)}
function th(a,b){if(!a.d){return}rh(a);YA(b,new li(a.b))}
function oi(a,b){if(null==b){throw new du(a+' cannot be null')}}
function ni(a,b){oi(a,b);if(0==qu(b).length){throw new St(a+' cannot be empty')}}
function li(a){ke();this.g='A request timeout has expired after '+a+' ms'}
function Kh(a,b){return $wnd.setTimeout(hC(function(){a.J()}),b)}
function Wh(a){ni('httpMethod',a);ni('url',VD);this.c=a;this.e=VD}
function rh(a){var b;if(a.d){b=a.d;a.d=null;lt(b);b.abort();!!a.c&&Gh(a.c)}}
function rp(a,b){var c,d,e;a.e=true;a.f=b;e=a.b;a.b=null;for(d=new Ew(e);d.c<d.e.ib();){c=Ui(Cw(d),26);c.N(b)}}
function sh(a,b){var c,d,e;if(!a.d){return}!!a.c&&Gh(a.c);e=a.d;a.d=null;c=uh(e);if(c!=null){d=new zc(c);b.b.R(d)}else{new Ah;b.b.S(null)}}
function Rh(){Rh=gC;new bi('DELETE');new bi(sC);new bi('HEAD');Qh=new bi('POST');new bi('PUT')}
function ii(a){ke();this.g='The URL '+a+' is invalid or violates the same-origin security restriction'}
function Hh(a,b){if(b<=0){throw new St('must be positive')}a.d?Ih(a.e):Jh(a.e);ex(Eh,a);a.d=false;a.e=Kh(a,b);ax(Eh,a)}
function sc(a,b){if(a.f){throw new Wt("Can't overwrite cause")}if(b==a){throw new St('Self-causation not permitted')}a.f=b;return a}
function PA(a){var b;b=null;switch(a){case 0:b='forward';break;case 1:b='backward';break;case 2:b=bD;break;case 3:b='right';}return b}
function vh(a,b,c){if(!a){throw new cu}if(!c){throw new cu}if(b<0){throw new Rt}this.b=b;this.d=a;if(b>0){this.c=new Nh(this,c);Hh(this.c,b)}else{this.c=null}}
function QA(b,c){var a,d,e,f;try{f=new Vh((Rh(),Qh));ni('header',WD);ni(XD,YD);!f.b&&(f.b=new Bx);f.b.nb(WD,YD);d=new Ju;Iu(Iu(Iu(Iu(Iu((d.b.b+='cmd',d),gD),b),AC),XD),gD).b.b+=-1;Th(f,d.b.b,new ZA(c))}catch(a){a=Sm(a);if(Wi(a,12)){e=a;c.R(e)}else throw a}}
function Uh(b,c){var a,d,e,f;if(!!b.b&&b.b.ib()>0){for(f=b.b.lb()._();f.bb();){e=Ui(f.cb(),48);try{ot(c,Ui(e.tb(),1),Ui(e.ub(),1))}catch(a){a=Sm(a);if(Wi(a,2)){d=a;throw new fi((d.d==null&&Cc(d),d.d))}else throw a}}}else{c.setRequestHeader('Content-Type','text/plain; charset=utf-8')}}
function Sh(b,c,d){var a,e,f,g,h;h=pt();try{mt(h,b.c,b.e)}catch(a){a=Sm(a);if(Wi(a,2)){e=a;g=new ii(b.e);sc(g,new fi((e.d==null&&Cc(e),e.d)));throw g}else throw a}Uh(b,h);f=new vh(h,b.d,d);nt(h,new Zh(f,d));try{h.send(c)}catch(a){a=Sm(a);if(Wi(a,2)){e=a;throw new fi((e.d==null&&Cc(e),e.d))}else throw a}return f}
function uh(b){try{if(b.status===undefined){return 'XmlHttpRequest.status == undefined, please see Safari bug http://bugs.webkit.org/show_bug.cgi?id=3810 for more details'}return null}catch(a){return 'Unable to read XmlHttpRequest.status; likely causes are a networking error or bad cross-domain request. Please see https://bugzilla.mozilla.org/show_bug.cgi?id=238559 for more details'}}
var WD='Content-type',YD='application/x-www-form-urlencoded',ZD='com.google.gwt.http.client.',VD='command',XD='value';_=vh.prototype=qh.prototype=new Lb;_.gC=function wh(){return Wj};_.b=0;_.c=null;_.d=null;_=yh.prototype=new Lb;_.gC=function zh(){return Xj};_=Ah.prototype=xh.prototype=new yh;_.gC=function Bh(){return Oj};_=Dh.prototype=new Lb;_.J=function Lh(){this.d||ex(Eh,this);th(this.b,this.c)};_.gC=function Mh(){return pk};_.cM={24:1};_.d=false;_.e=0;var Eh;_=Nh.prototype=Ch.prototype=new Dh;_.gC=function Oh(){return Pj};_.cM={24:1};_.b=null;_.c=null;_=Vh.prototype=Ph.prototype=new Lb;_.gC=function Xh(){return Sj};_.b=null;_.c=null;_.d=0;_.e=null;var Qh;_=Zh.prototype=Yh.prototype=new Lb;_.gC=function $h(){return Qj};_.D=function _h(a){if(a.readyState==4){lt(a);sh(this.c,this.b)}};_.b=null;_.c=null;_=bi.prototype=ai.prototype=new Lb;_.gC=function ci(){return Rj};_.tS=function di(){return this.b};_.b=null;_=fi.prototype=ei.prototype=new qc;_.gC=function gi(){return Tj};_.cM={12:1,36:1,43:1};_=ii.prototype=hi.prototype=new ei;_.gC=function ji(){return Uj};_.cM={12:1,36:1,43:1};_=li.prototype=ki.prototype=new ei;_.gC=function mi(){return Vj};_.cM={12:1,36:1,43:1};_=Wo.prototype=Vo.prototype=new Lb;_.gC=function Xo(){return ok};_.H=function Yo(a){while((Fh(),Eh).c>0){Gh(Ui(bx(Eh,0),24))}};_.cM={7:1,10:1};_=Rt.prototype=Qt.prototype;_=Yz.prototype;_.R=function _z(a){cp(a.y())};_.S=function aA(a){};_=bA.prototype;_.R=function eA(a){cp(a.y())};_.S=function fA(a){};_=EA.prototype;_.R=function HA(a){cp(a.y())};_.S=function IA(a){};_=JA.prototype;_.R=function MA(a){cp(a.y())};_.S=function NA(a){};_=RA.prototype=OA.prototype=new Lb;_.Bb=function SA(a){QA('cutter_off',a)};_.gC=function TA(){return um};_.Cb=function UA(a){QA('cutter_on',a)};_.Db=function VA(a,b){var c;c=PA(a);QA(c,b)};_.Eb=function WA(a){QA('stop',a)};_=ZA.prototype=XA.prototype=new Lb;_.gC=function $A(){return tm};_.b=null;_=gB.prototype;_.z=function iB(){rp(this.b,new RA)};var pk=Jt(LD,'Timer'),Wj=Jt(ZD,'Request'),Xj=Jt(ZD,'Response'),Oj=Jt(ZD,'Request$1'),Pj=Jt(ZD,'Request$3'),Sj=Jt(ZD,'RequestBuilder'),Qj=Jt(ZD,'RequestBuilder$1'),Rj=Jt(ZD,'RequestBuilder$Method'),Tj=Jt(ZD,'RequestException'),Uj=Jt(ZD,'RequestPermissionException'),Vj=Jt(ZD,'RequestTimeoutException'),ok=Jt(LD,'Timer$1'),um=Jt(SD,'RemoteService'),tm=Jt(SD,'RemoteService$1');hC(dd)(1);