;;; ***************************
;;; * DEFTEMPLATES & DEFFACTS *
;;; ***************************

(deftemplate UI-state
    (slot id (default-dynamic (gensym*)))
    (slot display)
    (slot relation-asserted (default none))
    (slot response (default none))
    (multislot valid-answers)
    (slot state (default middle))
)
   
(deftemplate state-list
    (slot current)
    (multislot sequence)
)
  
(deffacts startup
    (state-list)
)
   
;;;****************
;;;* STARTUP RULE *
;;;****************

(defrule system-banner ""
    =>
    (assert
        (UI-state
            (display Title)
            (relation-asserted start)
            (state initial)
            (valid-answers)
        )
    )
)

;;;***************
;;;* QUERY RULES *
;;;***************

(defrule determine-is-alone ""
    (logical (start))
    =>
    (assert
        (UI-state
            (display IsAloneQuestion)
            (relation-asserted is-alone)
            (response No)
            (valid-answers No Yes)
        )
   )
)

(defrule determine-is-at-home ""
    (logical (is-alone Yes))
    =>
    (assert
        (UI-state
            (display IsAtHomeQuestion)
            (relation-asserted is-at-home)
            (response No)
            (valid-answers No Yes)
        )
   )
)

(defrule determine-is-on-phone ""
    (logical (is-at-home Yes))
    =>
    (assert
        (UI-state
            (display IsOnPhoneQuestion)
            (relation-asserted is-on-phone)
            (response No)
            (valid-answers No Yes)
        )
   )
)

(defrule determine-is-asleep ""
    (logical (is-on-phone No))
    =>
    (assert
        (UI-state
            (display IsAsleepQuestion)
            (relation-asserted is-asleep)
            (response No)
            (valid-answers No Yes)
        )
   )
)

(defrule determine-is-babysitting ""
    (logical (is-asleep No))
    =>
    (assert
        (UI-state
            (display IsBabysittingQuestion)
            (relation-asserted is-babysitting)
            (response No)
            (valid-answers No Yes)
        )
   )
)

(defrule determine-is-watching-a-vhs-tape ""
    (logical (is-babysitting No))
    =>
    (assert
        (UI-state
            (display IsWatchingAVhsTapeQuestion)
            (relation-asserted is-watching-a-vhs-tape)
            (response No)
            (valid-answers No Yes)
        )
   )
)

(defrule determine-is-in-a-hotel ""
    (logical (is-at-home No))
    =>
    (assert
        (UI-state
            (display IsInAHotelQuestion)
            (relation-asserted is-in-a-hotel)
            (response No)
            (valid-answers No Yes)
        )
   )
)

(defrule determine-is-in-the-shower ""
    (logical (is-in-a-hotel Yes))
    =>
    (assert
        (UI-state
            (display IsInTheShowerQuestion)
            (relation-asserted is-in-the-shower)
            (response No)
            (valid-answers No Yes)
        )
   )
)

(defrule determine-is-in-a-hedge-maze ""
    (logical (is-in-a-hotel No))
    =>
    (assert
        (UI-state
            (display IsInAHedgeMazeQuestion)
            (relation-asserted is-in-a-hedge-maze)
            (response No)
            (valid-answers No Yes)
        )
   )
)

(defrule determine-is-separated-from-his-group ""
    (logical (is-in-a-hedge-maze No))
    =>
    (assert
        (UI-state
            (display WereYouSeparatedFromYourGroupQuestion)
            (relation-asserted is-separated-from-his-group)
            (response Yes)
            (valid-answers Yes)
        )
   )
)

;;;****************
;;;* ANSWER RULES *
;;;****************

(defrule stabbed-to-death-by-ghostface ""
    (logical (is-on-phone Yes))
    =>
    (assert
        (UI-state
            (display StabbedToDeathByGhostfaceAnswer)
            (state final)
        )
    )
)

(defrule murdered-in-your-dream ""
    (logical (is-asleep Yes))
    =>
    (assert
        (UI-state
            (display MurderedInYourDreamAnswer)
            (state final)
        )
    )
)

(defrule stabbed-to-death-by-michael-myers ""
    (logical (is-babysitting Yes))
    =>
    (assert
        (UI-state
            (display StabbedToDeathByMichaelMyersAnswer)
            (state final)
        )
    )
)

(defrule mysteriously-die-7-days-later ""
    (logical (is-watching-a-vhs-tape Yes))
    =>
    (assert
        (UI-state
            (display MysteriouslyDie7DaysLaterAnswer)
            (state final)
        )
    )
)

(defrule you-have-netflix ""
    (logical (is-watching-a-vhs-tape No))
    =>
    (assert
        (UI-state
            (display YouHaveNetflixAnswer)
            (state final)
        )
    )
)

(defrule stabbed-to-death-in-your-birthday-suit ""
    (logical (is-in-the-shower Yes))
    =>
    (assert
        (UI-state
            (display StabbedToDeathInYourBirthdaySuitAnswer)
            (state final)
        )
    )
)

(defrule stuck-for-eternity-in-a-haunted-hotel-room ""
    (logical (is-in-the-shower No))
    =>
    (assert
        (UI-state
            (display StuckForEternityInAHauntedHotelRoomAnswer)
            (state final)
        )
    )
)

(defrule you-freeze-to-death-while-chasing-your-son ""
    (logical (is-in-a-hedge-maze Yes))
    =>
    (assert
        (UI-state
            (display YouFreezeToDeathWhileChasingYourSonAnswer)
            (state final)
        )
    )
)

(defrule as-good-as-dead ""
    (logical (is-separated-from-his-group Yes))
    =>
    (assert
        (UI-state
            (display AsGoodAsDeadAnswer)
            (state final)
        )
    )
)

;;;*************************
;;;* GUI INTERACTION RULES *
;;;*************************

(defrule ask-question

   (declare (salience 5))
   
   (UI-state (id ?id))
   
   ?f <- (state-list (sequence $?s&:(not (member$ ?id ?s))))
             
   =>
   
   (modify ?f (current ?id)
              (sequence ?id ?s))
   
   (halt))

(defrule handle-next-no-change-none-middle-of-chain

   (declare (salience 10))
   
   ?f1 <- (next ?id)

   ?f2 <- (state-list (current ?id) (sequence $? ?nid ?id $?))
                      
   =>
      
   (retract ?f1)
   
   (modify ?f2 (current ?nid))
   
   (halt))

(defrule handle-next-response-none-end-of-chain

   (declare (salience 10))
   
   ?f <- (next ?id)

   (state-list (sequence ?id $?))
   
   (UI-state (id ?id)
             (relation-asserted ?relation))
                   
   =>
      
   (retract ?f)

   (assert (add-response ?id)))   

(defrule handle-next-no-change-middle-of-chain

   (declare (salience 10))
   
   ?f1 <- (next ?id ?response)

   ?f2 <- (state-list (current ?id) (sequence $? ?nid ?id $?))
     
   (UI-state (id ?id) (response ?response))
   
   =>
      
   (retract ?f1)
   
   (modify ?f2 (current ?nid))
   
   (halt))

(defrule handle-next-change-middle-of-chain

   (declare (salience 10))
   
   (next ?id ?response)

   ?f1 <- (state-list (current ?id) (sequence ?nid $?b ?id $?e))
     
   (UI-state (id ?id) (response ~?response))
   
   ?f2 <- (UI-state (id ?nid))
   
   =>
         
   (modify ?f1 (sequence ?b ?id ?e))
   
   (retract ?f2))
   
(defrule handle-next-response-end-of-chain

   (declare (salience 10))
   
   ?f1 <- (next ?id ?response)
   
   (state-list (sequence ?id $?))
   
   ?f2 <- (UI-state (id ?id)
                    (response ?expected)
                    (relation-asserted ?relation))
                
   =>
      
   (retract ?f1)

   (if (neq ?response ?expected)
      then
      (modify ?f2 (response ?response)))
      
   (assert (add-response ?id ?response)))   

(defrule handle-add-response

   (declare (salience 10))
   
   (logical (UI-state (id ?id)
                      (relation-asserted ?relation)))
   
   ?f1 <- (add-response ?id ?response)
                
   =>
      
   (str-assert (str-cat "(" ?relation " " ?response ")"))
   
   (retract ?f1))   

(defrule handle-add-response-none

   (declare (salience 10))
   
   (logical (UI-state (id ?id)
                      (relation-asserted ?relation)))
   
   ?f1 <- (add-response ?id)
                
   =>
      
   (str-assert (str-cat "(" ?relation ")"))
   
   (retract ?f1))   

(defrule handle-prev

   (declare (salience 10))
      
   ?f1 <- (prev ?id)
   
   ?f2 <- (state-list (sequence $?b ?id ?p $?e))
                
   =>
   
   (retract ?f1)
   
   (modify ?f2 (current ?p))
   
   (halt))
