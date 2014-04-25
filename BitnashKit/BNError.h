//
//  BNError.h
//  BitnashKit
//
//  Created by Rich Collins on 4/16/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNObject.h"

@interface BNError : BNObject

@property NSString *description;
@property NSNumber *insufficientValue;

@end
