require 'java'
require 'signal_detector_listener'

class PlayerListener
  include javax.media.mscontrol.MediaEventListener

  def initialize
  end
 
  def onEvent(event) 
      puts "PlayerListener Received event"
      puts event
      
      player = event.get_source
      media_group = player.get_container
      if (event.is_successful && javax.media.mscontrol.mediagroup.PlayerEvent.PLAY_COMPLETED == event.get_event_type)
        # welcome message has ended, starting to listen for DTMF
        puts "Received PlayComplete event"
        begin
          signal_detector = media_group.get_signal_detector
          signal_detector.add_listener(SignalDetectorListener.new)
          signal_detector.receive_signals(1, nil, nil, nil)
        rescue javax.media.mscontrol.MsControlException => e
          e.backTrace
        end
      else
        puts "Player didn't complete successfully "
      end
   end
end